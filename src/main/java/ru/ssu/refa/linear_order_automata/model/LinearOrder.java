package ru.ssu.refa.linear_order_automata.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class LinearOrder {
    private final boolean resolution;
    private String reason;
    private Set<Pair<State>> orderOnStateSet;
    private Set<Pair<OutputSignal>> orderOnOutputSignalSet;

    public LinearOrder(boolean resolution, String reason) {
        this.resolution = resolution;
        this.reason = reason;
    }

    public LinearOrder(Set<Pair<State>> orderOnStateSet, Set<Pair<OutputSignal>> orderOnOutputSignalSet) {
        this.resolution = true;
        this.orderOnStateSet = orderOnStateSet;
        this.orderOnOutputSignalSet = orderOnOutputSignalSet;
    }
}
