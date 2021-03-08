package ru.ssu.refa.linear_order_automata.model;

import lombok.Data;

@Data
public class InputSignal {
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
