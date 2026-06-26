package com.trifasico.x50zo.exceptions;

/**
 * Unchecked exception thrown when an internal game state becomes inconsistent
 * or when game logic is invoked in a way that violates the engine's
 * preconditions.
 *
 * <p>This exception signals a <em>programming error</em> rather than a
 * foreseeable game condition, so it is unchecked. Examples include:</p>
 * <ul>
 *   <li>Attempting to start a turn when the game is already over.</li>
 *   <li>Advancing to the next player when no players remain.</li>
 *   <li>A computed table sum somehow dropping below zero due to a logic bug.</li>
 * </ul>
 *
 * <p>Callers should <strong>not</strong> routinely catch this exception;
 * instead, they should fix the code path that triggers it.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see InvalidPlayerCountException
 */
public class GameRuleViolationException extends GameException {

    /**
     * Constructs a {@code GameRuleViolationException} with a detail message
     * describing the violated invariant.
     *
     * @param message a description of the illegal state or misuse
     */
    public GameRuleViolationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code GameRuleViolationException} with a detail message
     * and an underlying cause.
     *
     * @param message a description of the illegal state or misuse
     * @param cause   the throwable that exposed the violation
     */
    public GameRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
