package ru.ssu.refa.linear_order_automata.service;

import ru.ssu.refa.linear_order_automata.exception.SymmetricRelationException;
import ru.ssu.refa.linear_order_automata.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAutomataUtilsService {

    List<Pair<State>> findAllStatePairs(List<State> states);

    Set<Pair<State>> getTransitionOrbit(Automaton automaton, Pair<State> inputPair,
                                        Map<Pair<State>, Orbit> orbitCache) throws SymmetricRelationException;

    Set<Pair<OutputSignal>> getOutputOrbit(Automaton automaton,
                                           Set<Pair<State>> transitionOrbit) throws SymmetricRelationException;

    Map<Pair<State>, Orbit> getOrbits(Automaton automaton, Collection<Pair<State>> allStatePairs) throws SymmetricRelationException;

    <T extends Comparable<T>> Set<Pair<T>> getTransitiveClosure(Set<Pair<T>> relation, Collection<T> elements) throws SymmetricRelationException;

    <T extends Comparable<T>> boolean isOrderConstructed(Set<Pair<T>> relation, int totalCount);

    <T extends Comparable<T>> Set<Pair<T>> revertRelation(Set<Pair<T>> relation);
}
