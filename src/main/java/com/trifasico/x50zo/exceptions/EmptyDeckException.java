package com.trifasico.x50zo.exceptions;

/**
 * Checked exception thrown when the deck is empty <em>and</em> the table pile
 * does not have enough cards to reshuffle into a new deck.
 *
 * <p>Under normal game rules the pile is reshuffled whenever the deck runs out,
 * so this situation should be extremely rare. It is modelled as a checked
 * exception because the caller must decide how to recover — for example, by
 * ending the game or declaring the remaining player(s) winners.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (deck.isEmpty() && tablePile.size() <= 1) {
 *     throw new EmptyDeckException();
 * }
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
public class EmptyDeckException extends Exception {

    /** Number of cards available in the table pile when the exception was raised. */
    private final int availablePileCards;

    /**
     * Constructs an {@code EmptyDeckException} indicating that both the deck
     * and the reshuffleable portion of the table pile are exhausted.
     *
     * @param availablePileCards the number of cards currently in the table pile
     *                           (including the top card that cannot be taken)
     */
    public EmptyDeckException(int availablePileCards) {
        super(String.format(
                "Deck is empty and the table pile has only %d card(s) — not enough to reshuffle.",
                availablePileCards
        ));
        this.availablePileCards = availablePileCards;
    }

    /**
     * Returns the number of cards that were in the table pile when this
     * exception was thrown.
     *
     * @return pile card count at the time of failure
     */
    public int getAvailablePileCards() {
        return availablePileCards;
    }
}
