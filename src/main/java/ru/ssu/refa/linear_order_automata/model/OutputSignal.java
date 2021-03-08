package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

@Data
public class OutputSignal implements Orderable {
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
