package ru.ssu.refa.linear_order_automata.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Pair<T extends Comparable<T>> implements Comparable<Pair<T>> {

    private final T left;
    private final T right;

    public Pair<T> getRevertedPair() {
        return new Pair<T>(right, left);
    }

    @Override
    public int compareTo(Pair<T> o) {
        int leftPart = left.compareTo(o.getLeft());
        int rightPart = right.compareTo(o.getRight());

        if (leftPart == rightPart) {
            return leftPart;
        }

        if (leftPart == 0) {
            return rightPart;
        }

        return leftPart;
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
