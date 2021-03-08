package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

@Data
public class State implements Comparable<State>, Orderable {
    private final Integer label;

    @Override
    public int compareTo(State o) {
        return label.compareTo(o.getLabel());
    }

    @Override
    public String toString() {
        return label.toString();
    }
}
