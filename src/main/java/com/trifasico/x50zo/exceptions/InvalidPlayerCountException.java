package com.trifasico.x50zo.exceptions;

/**
 * Unchecked exception thrown when an invalid number of players is provided
 * during game setup.
 *
 * <p>The game requires exactly 1 human player plus 1, 2, or 3 machine players,
 * giving a total player count between 2 and 4 (inclusive). Providing a count
 * outside this range is a configuration error and therefore unchecked.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (machineCount < 1 || machineCount > 3) {
 *     throw new InvalidPlayerCountException(machineCount);
 * }
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see GameRuleViolationException
 */
public class InvalidPlayerCountException extends GameException {

    /** The invalid machine-player count that was supplied. */
    private final int suppliedCount;

    /**
     * Constructs an {@code InvalidPlayerCountException} for the given count.
     *
     * @param suppliedCount the number of machine players that was requested;
     *                      must be between 1 and 3 for a valid game
     */
    public InvalidPlayerCountException(int suppliedCount) {
        super(String.format(
                "Invalid machine player count: %d. The game requires between 1 and 3 machine players.",
                suppliedCount
        ));
        this.suppliedCount = suppliedCount;
    }

    /**
     * Returns the invalid player count that triggered this exception.
     *
     * @return the supplied (invalid) machine player count
     */
    public int getSuppliedCount() {
        return suppliedCount;
    }
}
