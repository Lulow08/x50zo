package com.trifasico.x50zo.model;

/**
 * An immutable snapshot of the public game state visible to all players at
 * the moment a machine player must make a decision.
 *
 * <p>Passing a {@code GameState} to {@link com.trifasico.x50zo.model.players.MachineStrategy}
 * decouples the AI from the live game objects. The strategy receives exactly
 * the information a real player could observe: the current table sum, how many
 * cards remain in the deck, and how many opponents are still active.</p>
 *
 * Constructs a {@code GameState} snapshot.
 * @param tableSum           the running sum on the table at decision time
 * @param remainingDeckCards number of cards left in the draw pile
 * @param activePlayers      number of players still in the game (including
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */

public record GameState(int tableSum, int remainingDeckCards, int activePlayers) {
    /**
     * Returns the current table sum that the next card played must not push above 50.
     *
     * @return the table sum
     */
    @Override
    public int tableSum() {
        return tableSum;
    }

    /**
     * Returns the number of cards remaining in the draw pile.
     *
     * <p>A low count signals that the pile may be reshuffled soon, which the
     * strategy can factor into its aggression level.</p>
     *
     * @return remaining deck card count
     */
    @Override
    public int remainingDeckCards() {
        return remainingDeckCards;
    }

    /**
     * Returns the number of players (including this machine) still active.
     *
     * @return active player count
     */
    @Override
    public int activePlayers() {
        return activePlayers;
    }

    /**
     * Returns the margin left before the table sum would reach 50.
     *
     * <p>Convenience method used by scoring strategies to avoid repeating
     * {@code 50 - tableSum}.</p>
     *
     * @return how much room is left (50 − tableSum); may be negative if the
     * sum somehow exceeded 50 due to a bug
     */
    public int remainingMargin() {
        return 50 - tableSum;
    }

    @Override
    public String toString() {
        return "GameState[tableSum=" + tableSum
                + ", deckCards=" + remainingDeckCards
                + ", activePlayers=" + activePlayers + "]";
    }
}