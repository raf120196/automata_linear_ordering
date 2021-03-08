package ru.ssu.refa.linear_order_automata.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ssu.refa.linear_order_automata.exception.SymmetricRelationException;
import ru.ssu.refa.linear_order_automata.model.*;
import ru.ssu.refa.linear_order_automata.service.IAutomataUtilsService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutomataUtilsService implements IAutomataUtilsService {

    @Override
    public List<Pair<State>> findAllStatePairs(List<State> states) {
        List<Pair<State>> result = new ArrayList<>();
        for (State state1 : states) {
            for (State state2 : states) {
                if (state1.compareTo(state2) < 0) {
                    result.add(new Pair<>(state1, state2));
                }
            }
        }
        return result;
    }

    @Override
    public Set<Pair<State>> getTransitionOrbit(Automaton automaton, Pair<State> inputPair,
                                               Map<Pair<State>, Orbit> orbitCache) throws SymmetricRelationException {
        log.debug("getTransitionOrbit: inputPair - {}, orbitCache = {}", inputPair, orbitCache);

        Set<Pair<State>> orbit = new HashSet<>();
        orbit.add(inputPair);

        Set<Pair<State>> consideredPairs = new HashSet<>();
        boolean continueIterating;
        do {
            continueIterating = false;
            for (Pair<State> pair : new ArrayList<>(orbit)) {
                if (!consideredPairs.contains(pair)) {
                    for (InputSignal inputSignal : automaton.getInputSignals()) {
                        log.debug("pair - {}, inputSignal - {}", pair, inputSignal);
                        Pair<State> nextPair = new Pair<>(
                                automaton.getTargetState(pair.getLeft(), inputSignal),
                                automaton.getTargetState(pair.getRight(), inputSignal)
                        );
                        log.debug("nextPair - {}", nextPair);
                        if (!isPairFine(nextPair, orbit)) {
                            continue;
                        }
                        if (orbit.contains(nextPair.getRevertedPair())) {
                            handleSymmetricPair(nextPair, inputPair);
                        }

                        continueIterating = true;
                        orbit.add(nextPair);
                        log.debug("added next pair {} to orbit", nextPair);
                        if (orbitCache.containsKey(nextPair)) {
                            for (Pair<State> cachedPair : orbitCache.get(nextPair).getTransitionOrbit()) {
                                if (orbit.contains(cachedPair.getRevertedPair())) {
                                    handleSymmetricPair(cachedPair, inputPair);
                                }
                                orbit.add(cachedPair);
                                log.debug("added cached pair {} to orbit", cachedPair);
                            }

                            consideredPairs.add(nextPair);
                        }
                    }

                    consideredPairs.add(pair);
                }
            }
        } while (continueIterating);

        return orbit;
    }

    @Override
    public Set<Pair<OutputSignal>> getOutputOrbit(Automaton automaton, Set<Pair<State>> transitionOrbit) throws SymmetricRelationException {
        Set<Pair<OutputSignal>> orbit = new HashSet<>();
        for (Pair<State> pair : transitionOrbit) {
            for (InputSignal inputSignal : automaton.getInputSignals()) {
                Pair<OutputSignal> outputPair = new Pair<>(
                        automaton.getOutputSignal(pair.getLeft(), inputSignal),
                        automaton.getOutputSignal(pair.getRight(), inputSignal)
                );
                if (outputPair.getLeft() == null || outputPair.getRight() == null) {
                    continue;
                }
                if (orbit.contains(outputPair.getRevertedPair())) {
                    handleSymmetricPair(outputPair);
                }

                if (!outputPair.getLeft().equals(outputPair.getRight())) {
                    orbit.add(outputPair);
                }
            }
        }

        return orbit;
    }

    @Override
    public Map<Pair<State>, Orbit> getOrbits(Automaton automaton, Collection<Pair<State>> allStatePairs) throws SymmetricRelationException {
        Map<Pair<State>, Orbit> result = new ConcurrentHashMap<>();
        try {
            allStatePairs.parallelStream()
                    .forEach(ThrowingConsumer.throwingConsumerWrapper(pair -> {
                        Set<Pair<State>> transitionOrbit = getTransitionOrbit(automaton, pair, result);
                        Set<Pair<OutputSignal>> outputOrbit = getOutputOrbit(automaton, transitionOrbit);
                        result.put(pair, new Orbit(transitionOrbit, outputOrbit));
                    }));
        } catch (RuntimeSymmetricRelationException e) {
            throw new SymmetricRelationException(e.getCause());
        }

        return result;
    }

    @Override
    public <T extends Comparable<T>> Set<Pair<T>> getTransitiveClosure(Set<Pair<T>> relation, Collection<T> elements) throws SymmetricRelationException {
        log.trace("getTransitiveClosure: relation = {}, elements = {}", relation, elements);
        Map<T, Set<T>> adjacencyMap = new HashMap<>();
        for (Pair<T> pair : relation) {
            adjacencyMap.computeIfAbsent(pair.getLeft(), v -> new HashSet<>());
            adjacencyMap.get(pair.getLeft()).add(pair.getRight());
        }
        log.trace("getTransitiveClosure: adjacencyMap = {}", adjacencyMap);

        Map<T, Set<T>> transitiveClosureMap = new HashMap<>();
        elements.forEach(t -> transitiveClosureMap.computeIfAbsent(t, v -> new HashSet<>()));
        for (T t : elements) {
            dfsTransitive(t, t, adjacencyMap, transitiveClosureMap);
        }
        log.trace("getTransitiveClosure: transitiveClosureMap = {}", transitiveClosureMap);

        Set<Pair<T>> transitivelyClosedSet = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : transitiveClosureMap.entrySet()) {
            entry.getValue().forEach(t -> transitivelyClosedSet.add(new Pair<>(entry.getKey(), t)));
        }
        return transitivelyClosedSet;
    }

    @Override
    public <T extends Comparable<T>> boolean isOrderConstructed(Set<Pair<T>> relation, int totalCount) {
        return (totalCount * totalCount + totalCount) / 2 == relation.size();
    }

    @Override
    public <T extends Comparable<T>> Set<Pair<T>> revertRelation(Set<Pair<T>> relation) {
        return relation.stream()
                .map(Pair::getRevertedPair)
                .collect(Collectors.toSet());
    }

    private <T extends Comparable<T>> void dfsTransitive(T from, T to,
                                                         Map<T, Set<T>> adjacencyMap, Map<T, Set<T>> transitiveClosureMap) throws SymmetricRelationException {
        log.trace("dfsUtil: from = {}, to = {}", from, to);
        if (!from.equals(to) && transitiveClosureMap.get(to).contains(from)) {
            throw new SymmetricRelationException();
        }
        transitiveClosureMap.get(from).add(to);
        if (adjacencyMap.containsKey(to)) {
            for (T nextTo : adjacencyMap.get(to)) {
                if (!transitiveClosureMap.get(from).contains(nextTo)) {
                    dfsTransitive(from, nextTo, adjacencyMap, transitiveClosureMap);
                }
            }
        }
    }

    private void dfsConnectedComponents(State state, Map<State, Set<State>> adjacencyMap,
                                        List<State> component, Set<State> visited) {
        component.add(state);
        visited.add(state);
        if (adjacencyMap.containsKey(state)) {
            adjacencyMap.get(state)
                    .stream()
                    .filter(nextState -> !visited.contains(nextState))
                    .forEach(nextState -> dfsConnectedComponents(nextState, adjacencyMap, component, visited));
        }
    }

    private boolean isPairFine(Pair<State> pair, Set<Pair<State>> orbit) {
        return pair.getLeft() != null && pair.getRight() != null &&
                !pair.getLeft().equals(pair.getRight()) && !orbit.contains(pair);
    }

    private void handleSymmetricPair(Pair<?> pair) throws SymmetricRelationException {
        handleSymmetricPair(pair, null);
    }

    private void handleSymmetricPair(Pair<?> pair, Pair<?> inputPair) throws SymmetricRelationException {
        String message = "Found symmetric pair for pair " + pair;
        if (inputPair != null) {
            message += " (inputPair - " + inputPair + ")";
        }
        log.error("getTransitionOrbit: {}", message);
        throw new SymmetricRelationException(message);
    }

    private interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;

        static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
            return i -> {
                try {
                    throwingConsumer.accept(i);
                } catch (Exception e) {
                    throw new RuntimeSymmetricRelationException(e);
                }
            };
        }
    }

    private static class RuntimeSymmetricRelationException extends RuntimeException {
        public RuntimeSymmetricRelationException(Throwable cause) {
            super(cause);
        }
    }
}
