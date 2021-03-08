package ru.ssu.refa.linear_order_automata.service.impl;

import org.junit.jupiter.api.Test;
import ru.ssu.refa.linear_order_automata.model.Automaton;
import ru.ssu.refa.linear_order_automata.model.InputSignal;
import ru.ssu.refa.linear_order_automata.model.OutputSignal;
import ru.ssu.refa.linear_order_automata.model.State;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AutomataOrderingServiceTest {
    @Test
    void test_1() {
        AutomataUtilsService automataUtilsService = new AutomataUtilsService();
        AutomataOrderingService automataOrderingService = new AutomataOrderingService(automataUtilsService);

        State state1 = new State(1);
        State state2 = new State(2);
        State state3 = new State(3);
        State state4 = new State(4);
        State state5 = new State(5);
        State state6 = new State(6);
        List<State> states = new ArrayList<>();
        states.add(state1);
        states.add(state2);
        states.add(state3);
        states.add(state4);
        states.add(state5);
        states.add(state6);

        InputSignal inputSignal1 = new InputSignal("a");
        InputSignal inputSignal2 = new InputSignal("b");
        List<InputSignal> inputSignals = new ArrayList<>();
        inputSignals.add(inputSignal1);
        inputSignals.add(inputSignal2);

        OutputSignal outputSignal1 = new OutputSignal("x");
        OutputSignal outputSignal2 = new OutputSignal("y");
        OutputSignal outputSignal3 = new OutputSignal("z");
        OutputSignal outputSignal4 = new OutputSignal("w");
        OutputSignal outputSignal5 = new OutputSignal("p");
        OutputSignal outputSignal6 = new OutputSignal("t");
        List<OutputSignal> outputSignals = new ArrayList<>();
        outputSignals.add(outputSignal1);
        outputSignals.add(outputSignal2);
        outputSignals.add(outputSignal3);
        outputSignals.add(outputSignal4);
        outputSignals.add(outputSignal5);
        outputSignals.add(outputSignal6);

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
        transitions.get(state4).put(inputSignal2, state4);
        transitions.put(state6, new HashMap<>());
        transitions.get(state6).put(inputSignal1, state5);

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
        automataOrderingService.orderAutomaton(automaton);
    }
}