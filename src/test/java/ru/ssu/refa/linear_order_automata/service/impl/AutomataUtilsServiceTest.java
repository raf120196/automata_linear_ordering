package ru.ssu.refa.linear_order_automata.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.ssu.refa.linear_order_automata.exception.SymmetricRelationException;
import ru.ssu.refa.linear_order_automata.model.*;
import ru.ssu.refa.linear_order_automata.service.IAutomataUtilsService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AutomataUtilsServiceTest {

    private IAutomataUtilsService automataUtilsService;

    private final State state1 = new State(1);
    private final State state2 = new State(2);
    private final State state3 = new State(3);
    private final State state4 = new State(4);
    private final State state5 = new State(5);
    private final State state6 = new State(6);
    private List<State> states;

    private final InputSignal inputSignal1 = new InputSignal("a");
    private final InputSignal inputSignal2 = new InputSignal("b");
    List<InputSignal> inputSignals;

    private final OutputSignal outputSignal1 = new OutputSignal("x");
    private final OutputSignal outputSignal2 = new OutputSignal("y");
    private final OutputSignal outputSignal3 = new OutputSignal("z");
    private final OutputSignal outputSignal4 = new OutputSignal("w");
    private final OutputSignal outputSignal5 = new OutputSignal("p");
    private final OutputSignal outputSignal6 = new OutputSignal("t");
    List<OutputSignal> outputSignals;

    @BeforeEach
    void setup() {
        automataUtilsService = new AutomataUtilsService();

        states = new ArrayList<>();
        states.add(state1);
        states.add(state2);
        states.add(state3);
        states.add(state4);
        states.add(state5);
        states.add(state6);

        inputSignals = new ArrayList<>();
        inputSignals.add(inputSignal1);
        inputSignals.add(inputSignal2);

        outputSignals = new ArrayList<>();
        outputSignals.add(outputSignal1);
        outputSignals.add(outputSignal2);
        outputSignals.add(outputSignal3);
        outputSignals.add(outputSignal4);
        outputSignals.add(outputSignal5);
        outputSignals.add(outputSignal6);
    }

    @Test
    public void getOrbitPositive_no_cache() throws SymmetricRelationException {
        Map<State, Map<InputSignal, State>> transitions = new HashMap<>();
        transitions.put(state1, new HashMap<>());
        transitions.get(state1).put(inputSignal1, state3);
        transitions.get(state1).put(inputSignal2, state4);
        transitions.put(state2, new HashMap<>());
        transitions.get(state2).put(inputSignal1, state4);
        transitions.get(state2).put(inputSignal2, state4);
        transitions.put(state3, new HashMap<>());
        transitions.get(state3).put(inputSignal1, state1);
        transitions.get(state3).put(inputSignal2, state3);
        transitions.put(state4, new HashMap<>());
        transitions.get(state4).put(inputSignal1, state5);
        transitions.get(state4).put(inputSignal2, state5);
        transitions.put(state5, new HashMap<>());
        transitions.get(state5).put(inputSignal1, state6);
        transitions.get(state5).put(inputSignal2, state6);
        transitions.put(state6, new HashMap<>());
        transitions.get(state6).put(inputSignal1, state6);
        transitions.get(state6).put(inputSignal2, state6);

        Map<State, Map<InputSignal, OutputSignal>> outputs = new HashMap<>();
        outputs.put(state1, new HashMap<>());
        outputs.get(state1).put(inputSignal1, outputSignal1);
        outputs.get(state1).put(inputSignal2, outputSignal2);
        outputs.put(state2, new HashMap<>());
        outputs.get(state2).put(inputSignal1, outputSignal2);
        outputs.get(state2).put(inputSignal2, outputSignal2);
        outputs.put(state3, new HashMap<>());
        outputs.get(state3).put(inputSignal1, outputSignal1);
        outputs.get(state3).put(inputSignal2, outputSignal1);
        outputs.put(state4, new HashMap<>());
        outputs.get(state4).put(inputSignal1, outputSignal3);
        outputs.get(state4).put(inputSignal2, outputSignal3);
        outputs.put(state5, new HashMap<>());
        outputs.get(state5).put(inputSignal1, outputSignal4);
        outputs.get(state5).put(inputSignal2, outputSignal4);
        outputs.put(state6, new HashMap<>());
        outputs.get(state6).put(inputSignal1, outputSignal4);
        outputs.get(state6).put(inputSignal2, outputSignal4);

        Automaton automaton = new Automaton(states, inputSignals, outputSignals, transitions, outputs);
        Set<Pair<State>> expectedResult = new HashSet<>();
        expectedResult.add(new Pair<>(state1, state2));
        expectedResult.add(new Pair<>(state3, state4));
        expectedResult.add(new Pair<>(state1, state5));
        expectedResult.add(new Pair<>(state3, state5));
        expectedResult.add(new Pair<>(state3, state6));
        expectedResult.add(new Pair<>(state4, state6));
        expectedResult.add(new Pair<>(state1, state6));
        expectedResult.add(new Pair<>(state5, state6));
        assertEquals(expectedResult, automataUtilsService.getTransitionOrbit(automaton, new Pair<>(state1, state2), Collections.emptyMap()));
    }

    @Test
    public void getOrbit_positive_with_cache() throws SymmetricRelationException {
        Map<State, Map<InputSignal, State>> transitions = new HashMap<>();
        transitions.put(state1, new HashMap<>());
        transitions.get(state1).put(inputSignal1, state3);
        transitions.get(state1).put(inputSignal2, state4);
        transitions.put(state2, new HashMap<>());
        transitions.get(state2).put(inputSignal1, state4);
        transitions.get(state2).put(inputSignal2, state4);
        transitions.put(state3, new HashMap<>());
        transitions.get(state3).put(inputSignal1, state1);
        transitions.get(state3).put(inputSignal2, state3);
        transitions.put(state4, new HashMap<>());
        transitions.get(state4).put(inputSignal1, state5);
        transitions.get(state4).put(inputSignal2, state5);
        transitions.put(state5, new HashMap<>());
        transitions.get(state5).put(inputSignal1, state6);
        transitions.get(state5).put(inputSignal2, state6);
        transitions.put(state6, new HashMap<>());
        transitions.get(state6).put(inputSignal1, state6);
        transitions.get(state6).put(inputSignal2, state6);

        Map<State, Map<InputSignal, OutputSignal>> outputs = new HashMap<>();
        outputs.put(state1, new HashMap<>());
        outputs.get(state1).put(inputSignal1, outputSignal1);
        outputs.get(state1).put(inputSignal2, outputSignal2);
        outputs.put(state2, new HashMap<>());
        outputs.get(state2).put(inputSignal1, outputSignal2);
        outputs.get(state2).put(inputSignal2, outputSignal2);
        outputs.put(state3, new HashMap<>());
        outputs.get(state3).put(inputSignal1, outputSignal1);
        outputs.get(state3).put(inputSignal2, outputSignal1);
        outputs.put(state4, new HashMap<>());
        outputs.get(state4).put(inputSignal1, outputSignal3);
        outputs.get(state4).put(inputSignal2, outputSignal3);
        outputs.put(state5, new HashMap<>());
        outputs.get(state5).put(inputSignal1, outputSignal4);
        outputs.get(state5).put(inputSignal2, outputSignal4);
        outputs.put(state6, new HashMap<>());
        outputs.get(state6).put(inputSignal1, outputSignal4);
        outputs.get(state6).put(inputSignal2, outputSignal4);

        Automaton automaton = new Automaton(states, inputSignals, outputSignals, transitions, outputs);
        Set<Pair<State>> expectedResult = new HashSet<>();
        expectedResult.add(new Pair<>(state1, state2));
        expectedResult.add(new Pair<>(state3, state4));
        expectedResult.add(new Pair<>(state1, state5));
        expectedResult.add(new Pair<>(state3, state5));
        expectedResult.add(new Pair<>(state3, state6));
        expectedResult.add(new Pair<>(state4, state6));
        expectedResult.add(new Pair<>(state1, state6));
        expectedResult.add(new Pair<>(state5, state6));

        Map<Pair<State>, Orbit> cache = new HashMap<>();
        Pair<State> key = new Pair<>(state4, state6);
        cache.put(key, new Orbit(Collections.singleton(new Pair<>(state5, state6)), Collections.emptySet()));

        assertEquals(expectedResult, automataUtilsService.getTransitionOrbit(automaton, new Pair<>(state1, state2), cache));
    }

    @Test
    public void getOrbit_negative_no_cache() {
        Map<State, Map<InputSignal, State>> transitions = new HashMap<>();
        transitions.put(state1, new HashMap<>());
        transitions.get(state1).put(inputSignal1, state3);
        transitions.get(state1).put(inputSignal2, state4);
        transitions.put(state2, new HashMap<>());
        transitions.get(state2).put(inputSignal1, state4);
        transitions.get(state2).put(inputSignal2, state4);
        transitions.put(state3, new HashMap<>());
        transitions.get(state3).put(inputSignal1, state1);
        transitions.get(state3).put(inputSignal2, state2);
        transitions.put(state4, new HashMap<>());
        transitions.get(state4).put(inputSignal1, state5);
        transitions.get(state4).put(inputSignal2, state1);
        transitions.put(state5, new HashMap<>());
        transitions.get(state5).put(inputSignal1, state6);
        transitions.get(state5).put(inputSignal2, state6);
        transitions.put(state6, new HashMap<>());
        transitions.get(state6).put(inputSignal1, state6);
        transitions.get(state6).put(inputSignal2, state6);

        Map<State, Map<InputSignal, OutputSignal>> outputs = new HashMap<>();
        outputs.put(state1, new HashMap<>());
        outputs.get(state1).put(inputSignal1, outputSignal1);
        outputs.get(state1).put(inputSignal2, outputSignal2);
        outputs.put(state2, new HashMap<>());
        outputs.get(state2).put(inputSignal1, outputSignal2);
        outputs.get(state2).put(inputSignal2, outputSignal2);
        outputs.put(state3, new HashMap<>());
        outputs.get(state3).put(inputSignal1, outputSignal1);
        outputs.get(state3).put(inputSignal2, outputSignal1);
        outputs.put(state4, new HashMap<>());
        outputs.get(state4).put(inputSignal1, outputSignal3);
        outputs.get(state4).put(inputSignal2, outputSignal3);
        outputs.put(state5, new HashMap<>());
        outputs.get(state5).put(inputSignal1, outputSignal4);
        outputs.get(state5).put(inputSignal2, outputSignal4);
        outputs.put(state6, new HashMap<>());
        outputs.get(state6).put(inputSignal1, outputSignal4);
        outputs.get(state6).put(inputSignal2, outputSignal4);

        Automaton automaton = new Automaton(states, inputSignals, outputSignals, transitions, outputs);
        assertThrows(SymmetricRelationException.class, () ->
                automataUtilsService.getTransitionOrbit(automaton, new Pair<>(state1, state2), Collections.emptyMap()));
    }

    @Test
    public void getOutputOrbit_positive() throws SymmetricRelationException {
        Map<State, Map<InputSignal, State>> transitions = new HashMap<>();
        transitions.put(state1, new HashMap<>());
        transitions.get(state1).put(inputSignal1, state2);
        transitions.get(state1).put(inputSignal2, state3);
        transitions.put(state2, new HashMap<>());
        transitions.get(state2).put(inputSignal1, state2);
        transitions.get(state2).put(inputSignal2, state2);
        transitions.put(state3, new HashMap<>());
        transitions.get(state3).put(inputSignal1, state4);
        transitions.get(state3).put(inputSignal2, state4);
        transitions.put(state4, new HashMap<>());
        transitions.get(state4).put(inputSignal1, state4);
        transitions.get(state4).put(inputSignal2, state5);

        Map<State, Map<InputSignal, OutputSignal>> outputs = new HashMap<>();
        outputs.put(state1, new HashMap<>());
        outputs.get(state1).put(inputSignal1, outputSignal1);
        outputs.get(state1).put(inputSignal2, outputSignal2);
        outputs.put(state2, new HashMap<>());
        outputs.get(state2).put(inputSignal1, outputSignal2);
        outputs.get(state2).put(inputSignal2, outputSignal2);
        outputs.put(state3, new HashMap<>());
        outputs.get(state3).put(inputSignal1, outputSignal4);
        outputs.get(state3).put(inputSignal2, outputSignal2);
        outputs.put(state4, new HashMap<>());
        outputs.get(state4).put(inputSignal1, outputSignal3);
        outputs.get(state4).put(inputSignal2, outputSignal4);

        Automaton automaton = new Automaton(states, inputSignals, outputSignals, transitions, outputs);
        Set<Pair<OutputSignal>> expectedResult = new HashSet<>();
        expectedResult.add(new Pair<>(outputSignal1, outputSignal4));
        expectedResult.add(new Pair<>(outputSignal2, outputSignal3));
        expectedResult.add(new Pair<>(outputSignal2, outputSignal4));
        expectedResult.add(new Pair<>(outputSignal4, outputSignal3));
        assertEquals(expectedResult, automataUtilsService.getOutputOrbit(automaton, automataUtilsService.getTransitionOrbit(
                automaton, new Pair<>(state1, state3), Collections.emptyMap())));
    }

    @Test
    public void getTransitiveClosure_negative() {
        Set<State> elements = new HashSet<>();
        elements.add(state1);
        elements.add(state2);
        elements.add(state3);
        elements.add(state4);
        Set<Pair<State>> set = new HashSet<>();
        set.add(new Pair<>(state1, state2));
        set.add(new Pair<>(state1, state3));
        set.add(new Pair<>(state2, state3));
        set.add(new Pair<>(state3, state1));
        set.add(new Pair<>(state3, state4));
        set.add(new Pair<>(state4, state4));
        assertThrows(SymmetricRelationException.class, () -> automataUtilsService.getTransitiveClosure(set, elements));
    }

    @Test
    public void getTransitiveClosure_positive() throws SymmetricRelationException {
        Set<State> elements = new HashSet<>();
        elements.add(state1);
        elements.add(state2);
        elements.add(state3);
        elements.add(state4);
        elements.add(state5);
        elements.add(state6);
        Set<Pair<State>> set = new HashSet<>();
        set.add(new Pair<>(state1, state2));
        set.add(new Pair<>(state2, state3));
        set.add(new Pair<>(state3, state6));
        set.add(new Pair<>(state4, state5));
        set.add(new Pair<>(state5, state3));
        Set<Pair<State>> expectedResult = new HashSet<>();
        expectedResult.add(new Pair<>(state1, state2));
        expectedResult.add(new Pair<>(state2, state3));
        expectedResult.add(new Pair<>(state3, state6));
        expectedResult.add(new Pair<>(state4, state5));
        expectedResult.add(new Pair<>(state5, state3));
        expectedResult.add(new Pair<>(state1, state3));
        expectedResult.add(new Pair<>(state1, state6));
        expectedResult.add(new Pair<>(state2, state6));
        expectedResult.add(new Pair<>(state4, state3));
        expectedResult.add(new Pair<>(state4, state6));
        expectedResult.add(new Pair<>(state5, state6));
        expectedResult.add(new Pair<>(state1, state1));
        expectedResult.add(new Pair<>(state2, state2));
        expectedResult.add(new Pair<>(state3, state3));
        expectedResult.add(new Pair<>(state4, state4));
        expectedResult.add(new Pair<>(state5, state5));
        expectedResult.add(new Pair<>(state6, state6));
        assertEquals(expectedResult, automataUtilsService.getTransitiveClosure(set, elements));
    }
}
