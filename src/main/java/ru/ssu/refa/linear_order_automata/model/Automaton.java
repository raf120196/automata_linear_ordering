package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Automaton {
    private final List<State> states;
    private final List<InputSignal> inputSignals;
    private final List<OutputSignal> outputSignals;
    private final Map<State, Map<InputSignal, State>> transitions;
    private final Map<State, Map<InputSignal, OutputSignal>> outputs;

    public Automaton(List<State> states,
                     List<InputSignal> inputSignals,
                     List<OutputSignal> outputSignals,
                     Map<State, Map<InputSignal, State>> transitions,
                     Map<State, Map<InputSignal, OutputSignal>> outputs) {
        this.states = states;
        this.inputSignals = inputSignals;
        this.outputSignals = outputSignals;
        this.transitions = transitions;
        this.outputs = outputs;
    }

    public State getTargetState(State state, InputSignal inputSignal) {
        if (transitions.containsKey(state)) {
            return transitions.get(state).get(inputSignal);
        }

        return null;
    }

    public OutputSignal getOutputSignal(State state, InputSignal inputSignal) {
        if (outputs.containsKey(state)) {
            return outputs.get(state).get(inputSignal);
        }

        return null;
    }
}
