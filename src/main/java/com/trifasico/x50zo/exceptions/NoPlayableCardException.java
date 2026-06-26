package com.trifasico.x50zo.exceptions;

/**
 * Checked exception thrown when a player cannot play <em>any</em> card from
 * their hand without exceeding the table sum of 50.
 *
 * <p>This is a <em>checked</em> exception because it is a foreseeable game
 * condition that the caller must explicitly handle — the player holding this
 * hand must be eliminated from the game according to the rules.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * boolean canPlay = hand.stream().anyMatch(c -> c.isPlayable(currentSum));
 * if (!canPlay) {
 *     throw new NoPlayableCardException(playerName, currentSum);
 * }
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see InvalidPlayException
 */
public class NoPlayableCardException extends Exception {

    /** Name of the player who has no valid move. */
    private final String playerName;

    /** The table sum that made all cards unplayable. */
    private final int currentSum;

    /**
     * Constructs a {@code NoPlayableCardException} for the specified player.
     *
     * @param playerName the name or identifier of the player with no valid move
     * @param currentSum the table sum at the time the exception was raised
     */
    public NoPlayableCardException(String playerName, int currentSum) {
        super(String.format(
                "Player '%s' has no playable card: every card in hand would exceed the table sum of %d.",
                playerName, currentSum
        ));
        this.playerName = playerName;
        this.currentSum = currentSum;
    }

    /**
     * Returns the name of the player that cannot move.
     *
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the table sum that blocked all possible plays.
     *
     * @return the current table sum
     */
    public int getCurrentSum() {
        return currentSum;
    }
}
