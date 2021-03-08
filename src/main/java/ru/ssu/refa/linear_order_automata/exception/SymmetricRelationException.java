package ru.ssu.refa.linear_order_automata.exception;

public class SymmetricRelationException extends Exception {
    public SymmetricRelationException() {
        super();
    }

    public SymmetricRelationException(String message) {
        super(message);
    }

    public SymmetricRelationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SymmetricRelationException(Throwable cause) {
        super(cause);
    }

    protected SymmetricRelationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
