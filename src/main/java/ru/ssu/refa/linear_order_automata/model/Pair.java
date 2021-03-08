package ru.ssu.refa.linear_order_automata.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Pair<T extends Orderable> {

    private final T left;
    private final T right;

    public Pair<T> getRevertedPair() {
        return new Pair<>(right, left);
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
