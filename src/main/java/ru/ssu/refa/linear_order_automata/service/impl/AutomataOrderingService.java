package ru.ssu.refa.linear_order_automata.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ssu.refa.linear_order_automata.exception.SymmetricRelationException;
import ru.ssu.refa.linear_order_automata.model.*;
import ru.ssu.refa.linear_order_automata.service.IAutomataOrderingService;
import ru.ssu.refa.linear_order_automata.service.IAutomataUtilsService;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

@Slf4j
@Service
public class AutomataOrderingService implements IAutomataOrderingService {

    private final IAutomataUtilsService automataUtilsService;

    public AutomataOrderingService(IAutomataUtilsService automataUtilsService) {
        this.automataUtilsService = automataUtilsService;
    }

    @Override
    public LinearOrder orderAutomaton(Automaton automaton) {
        List<Pair<State>> allStatePairs = automataUtilsService.findAllStatePairs(automaton.getStates());
        log.debug("allStatePairs: {}", allStatePairs);

        Map<Pair<State>, Orbit> orbits;
        try {
            orbits = automataUtilsService.getOrbits(automaton, allStatePairs);
            log.debug("orbits: {}", orbits);
        } catch (SymmetricRelationException e) {
            log.error("found symmetric pair during calculation of orbits: " + e.getMessage(), e);
            return new LinearOrder(false, e.getMessage());
        }

        List<Pair<State>> sortedStatePairs = sortPairsByOrbitPower(orbits);
        log.debug("sortedStatePairs: {}", sortedStatePairs);

        Set<Pair<State>> wRelation = new HashSet<>();
        automaton.getStates().forEach(state -> wRelation.add(new Pair<>(state, state)));

        Set<Pair<OutputSignal>> w1Relation = new HashSet<>();
        automaton.getOutputSignals().forEach(outputSignal -> w1Relation.add(new Pair<>(outputSignal, outputSignal)));

        Deque<Step> steps = new LinkedList<>();
        steps.push(new Step(wRelation, w1Relation));

        Pair<State> currentPair;
        while (true) {
            Step previousStep = steps.peek();
            log.debug("previousStep = {}", previousStep);
            if (automataUtilsService.isOrderConstructed(previousStep.wRelation, automaton.getStates().size())) {
                LinearOrder linearOrder = new LinearOrder(previousStep.wRelation, previousStep.w1Relation);
                log.debug("result = {}", linearOrder);
                return linearOrder;
            }

            Optional<Pair<State>> nextPair = selectNextPair(sortedStatePairs, previousStep.wRelation);
            if (nextPair.isEmpty()) {
                LinearOrder linearOrder = new LinearOrder(false, "Can't select next pair");
                log.debug("result = {}", linearOrder);
                return linearOrder;
            }
            currentPair = nextPair.get();
            log.debug("currentPair = {}", currentPair);

            try {
                Order order = tryToAddOrbits(previousStep, automaton, orbits.get(currentPair).getTransitionOrbit(),
                        orbits.get(currentPair).getOutputOrbit(), false);
                log.debug("order = {}", order);
                if (!order.isPairReverted()) {
                    steps.push(new Step(currentPair, false, order.getWRelation(), order.getW1Relation()));
                } else {
                    steps.push(new Step(currentPair.getRevertedPair(), true, order.getWRelation(), order.getW1Relation()));
                }
            } catch (SymmetricRelationException e) {
                log.error("back iteration");
                while (true) {
                    while (steps.peek().isReverted) {
                        log.debug("remove");
                        steps.pop();
                    }

                    if (steps.peek().currentPair == null) {
                        LinearOrder linearOrder = new LinearOrder(false, "Back to the root of stack");
                        log.debug("result = {}", linearOrder);
                        return linearOrder;
                    }

                    Step revertedStep = steps.pop();
                    Pair<State> pairOfRevertedStep = revertedStep.currentPair;
                    try {
                        Order order = tryToAddOrbits(steps.peek(), automaton, automataUtilsService.revertRelation(orbits.get(pairOfRevertedStep)
                                .getTransitionOrbit()), automataUtilsService.revertRelation(orbits.get(pairOfRevertedStep)
                                .getOutputOrbit()), true);
                        steps.push(new Step(pairOfRevertedStep.getRevertedPair(), true, order.getWRelation(), order.getW1Relation()));
                        break;
                    } catch (SymmetricRelationException ex) {
                        log.error("continue iteration");
                        // continue iteration
                    }
                }
            }
        }
    }

    private Order tryToAddOrbits(Step previousStep, Automaton automaton, Set<Pair<State>> transitionOrbit,
                                 Set<Pair<OutputSignal>> outputOrbit, boolean isReverted) throws SymmetricRelationException {
        Set<Pair<State>> wRelation = new HashSet<>(previousStep.wRelation);
        Set<Pair<OutputSignal>> w1Relation = new HashSet<>(previousStep.w1Relation);
        try {
            mergeRelations(wRelation, transitionOrbit);
            wRelation = automataUtilsService.getTransitiveClosure(wRelation, automaton.getStates());

            mergeRelations(w1Relation, outputOrbit);
            w1Relation = automataUtilsService.getTransitiveClosure(w1Relation, automaton.getOutputSignals());
        } catch (SymmetricRelationException e) {
            if (!isReverted) {
                return tryToAddOrbits(previousStep, automaton, automataUtilsService.revertRelation(transitionOrbit),
                        automataUtilsService.revertRelation(outputOrbit), true);
            } else {
                throw e;
            }
        }
        return new Order(wRelation, w1Relation, isReverted);
    }

    private <T extends Comparable<T>> void mergeRelations(Set<Pair<T>> wRelation, Set<Pair<T>> transitionOrbit) throws SymmetricRelationException {
        for (Pair<T> pair : transitionOrbit) {
            if (wRelation.contains(pair.getRevertedPair())) {
                throw new SymmetricRelationException("Can't merge relations as found symmetric pair for " + pair);
            }

            wRelation.add(pair);
        }
    }

    private Optional<Pair<State>> selectNextPair(List<Pair<State>> sortedStatePairs, Set<Pair<State>> wRelation) {
        return sortedStatePairs.stream()
                .filter(pair -> !wRelation.contains(pair) && !wRelation.contains(pair.getRevertedPair()))
                .findFirst();
    }

    private List<Pair<State>> sortPairsByOrbitPower(Map<Pair<State>, Orbit> orbits) {
        return orbits.keySet().stream()
                .sorted(comparingInt(x -> orbits.get(x).getTransitionOrbit().size() +
                        orbits.get(x).getOutputOrbit().size()).reversed())
                .collect(Collectors.toList());
    }

    private class Step {
        private Pair<State> currentPair;
        private boolean isReverted;
        private Set<Pair<State>> wRelation;
        private Set<Pair<OutputSignal>> w1Relation;

        public Step(Set<Pair<State>> wRelation, Set<Pair<OutputSignal>> w1Relation) {
            this.wRelation = wRelation;
            this.w1Relation = w1Relation;
            this.currentPair = null;
        }

        public Step(Pair<State> currentPair, boolean isReverted, Set<Pair<State>> wRelation, Set<Pair<OutputSignal>> w1Relation) {
            this.currentPair = currentPair;
            this.isReverted = isReverted;
            this.wRelation = wRelation;
            this.w1Relation = w1Relation;
        }

        @Override
        public String toString() {
            return "Step{" +
                    "currentPair=" + currentPair +
                    ", isReverted=" + isReverted +
                    ", wRelation=" + wRelation +
                    ", w1Relation=" + w1Relation +
                    '}';
        }
    }
}
