package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

@Data
public class OutputSignal implements Comparable<OutputSignal> {
    private final String name;

    @Override
    public int compareTo(OutputSignal o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
