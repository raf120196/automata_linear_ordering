package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

import java.util.Set;

@Data
public class Order {
    private final Set<Pair<State>> wRelation;
    private final Set<Pair<OutputSignal>> w1Relation;
    private final boolean pairReverted;
}
