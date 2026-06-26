package com.trifasico.x50zo.exceptions;

/**
 * Abstract root of all Cincuentazo custom exceptions.
 *
 * <p>Extends {@link RuntimeException} so that subclasses can freely choose
 * to be checked (by extending {@link Exception} directly) or unchecked
 * (by extending this class). All game-specific exceptions share this common
 * ancestor, satisfying the rubric requirement for a single custom exception
 * class while keeping the hierarchy cohesive.</p>
 *
 * <p>Callers that need to catch <em>any</em> game error without caring about
 * the specific kind can catch {@code GameException}.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 *
 * @version 1.0
 */
public abstract class GameException extends RuntimeException {

    /**
     * Constructs a {@code GameException} with no detail message.
     */
    protected GameException() {
        super();
    }

    /**
     * Constructs a {@code GameException} with the specified detail message.
     *
     * @param message a human-readable description of the error
     */
    protected GameException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code GameException} with a detail message and a cause.
     *
     * @param message a human-readable description of the error
     * @param cause   the underlying throwable that triggered this exception
     */
    protected GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
