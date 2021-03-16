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
        log.debug("orderAutomaton: automaton - {}", automaton);

        List<Pair<State>> allStatePairs = automataUtilsService.findAllStatePairs(automaton.getStates());
        log.trace("allStatePairs - {}", allStatePairs);

        Map<Pair<State>, Orbit> orbits;
        try {
            orbits = automataUtilsService.getOrbits(automaton, allStatePairs);
            log.trace("orbits - {}", orbits);
        } catch (SymmetricRelationException e) {
            log.error("Found symmetric pair during calculation of orbits: " + e.getMessage(), e);

            LinearOrder linearOrder = new LinearOrder(false,
                    "The property of antisymmetry is violated on the step of orbits calculation");
            log.debug("orderAutomaton: result - {}", linearOrder);
            return linearOrder;
        }

        List<Pair<State>> sortedStatePairs = sortPairsByOrbitPower(orbits);
        log.trace("sortedStatePairs - {}", sortedStatePairs);

        Set<Pair<State>> wRelation = new HashSet<>();
        automaton.getStates().forEach(state -> wRelation.add(new Pair<>(state, state)));
        Set<Pair<OutputSignal>> w1Relation = new HashSet<>();
        automaton.getOutputSignals().forEach(outputSignal -> w1Relation.add(new Pair<>(outputSignal, outputSignal)));
        log.trace("w and w1 are initialized: w - {}, w1 - {}", wRelation, w1Relation);

        int stepNo = 0;
        Deque<Step> steps = new LinkedList<>();
        steps.push(new Step(wRelation, w1Relation));
        log.trace("[{}] - stack is initiated", stepNo++);

        Pair<State> currentPair;
        while (true) {
            Step previousStep = steps.peek();
            log.trace("previousStep - {}", previousStep);

            // previousStep mayn't be null here, so we can skip an assertion
            if (automataUtilsService.isOrderConstructed(previousStep.wRelation, automaton.getStates().size())) {
                Set<Pair<OutputSignal>> lOrderOnOutputSet = automataUtilsService.doTopologicalSort(
                        automaton.getOutputSignals(), previousStep.w1Relation);
                LinearOrder linearOrder = new LinearOrder(previousStep.wRelation, lOrderOnOutputSet);
                log.debug("orderAutomaton: result - {}", linearOrder);
                return linearOrder;
            }

            Optional<Pair<State>> nextPair = selectNextPair(sortedStatePairs, previousStep.wRelation);
            if (nextPair.isEmpty()) {
                LinearOrder linearOrder = new LinearOrder(false, "Can't select next pair for order construction");
                log.debug("orderAutomaton: result - {}", linearOrder);
                return linearOrder;
            }
            currentPair = nextPair.get();
            log.trace("currentPair - {}", currentPair);

            try {
                Order partialOrder = tryToAddOrbits(previousStep, automaton, orbits.get(currentPair).getTransitionOrbit(),
                        orbits.get(currentPair).getOutputOrbit(), false);
                log.trace("partialOrder - {}", partialOrder);
                if (!partialOrder.isPairReverted()) {
                    steps.push(new Step(currentPair, false, partialOrder.getWRelation(), partialOrder.getW1Relation()));
                    log.trace("[{}] - added straight pair {} to stack", stepNo++, currentPair);
                } else {
                    Pair<State> currentRevertedPair = currentPair.getRevertedPair();
                    steps.push(new Step(currentRevertedPair, true, partialOrder.getWRelation(), partialOrder.getW1Relation()));
                    log.trace("[{}] - added reverted pair {} to stack", stepNo++, currentRevertedPair);
                }
            } catch (SymmetricRelationException e) {
                log.trace("Straight and reverted pair {} violates antisymmetry -> roll back", currentPair);

                while (true) {
                    // initial step has isReverted = false, so we can skip an assertion
                    while (steps.peek().isReverted) {
                        steps.pop();
                        log.trace("[{}] - removed pair {} from stack", stepNo++, currentPair);
                    }

                    if (steps.peek().currentPair == null) {
                        LinearOrder linearOrder = new LinearOrder(false,
                                "Rolled back to the root of stack, all options violate antisymmetry");
                        log.debug("orderAutomaton: result - {}", linearOrder);
                        return linearOrder;
                    }

                    Step stepToRevert = steps.pop();
                    log.trace("stepToRevert - {}", stepToRevert);
                    Pair<State> pairOfStepToRevert = stepToRevert.currentPair;
                    try {
                        Order partialOrder = tryToAddOrbits(steps.peek(), automaton,
                                automataUtilsService.revertRelation(orbits.get(pairOfStepToRevert).getTransitionOrbit()),
                                automataUtilsService.revertRelation(orbits.get(pairOfStepToRevert).getOutputOrbit()),
                                true
                        );
                        log.trace("partialOrder - {}", partialOrder);
                        steps.push(new Step(pairOfStepToRevert.getRevertedPair(), true, partialOrder.getWRelation(), partialOrder.getW1Relation()));
                        break;
                    } catch (SymmetricRelationException ex) {
                        log.error("Reverted pair violates antisymmetry, continue rolling back");
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

    private <T extends Orderable> void mergeRelations(Set<Pair<T>> wRelation, Set<Pair<T>> transitionOrbit) throws SymmetricRelationException {
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

    private static class Step {
        private final Pair<State> currentPair;
        private final boolean isReverted;
        private final Set<Pair<State>> wRelation;
        private final Set<Pair<OutputSignal>> w1Relation;

        public Step(Set<Pair<State>> wRelation, Set<Pair<OutputSignal>> w1Relation) {
            this.wRelation = wRelation;
            this.w1Relation = w1Relation;
            this.currentPair = null;
            this.isReverted = false;
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
                    "\nw=" + wRelation +
                    "\nw1=" + w1Relation +
                    '}';
        }
    }
}
