package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

import java.util.Set;

@Data
public class Orbit {
    private final Set<Pair<State>> transitionOrbit;
    private final Set<Pair<OutputSignal>> outputOrbit;

    @Override
    public String toString() {
        return "transitionOrbit: " + transitionOrbit + ", outputOrbit=" + outputOrbit;
    }
}
