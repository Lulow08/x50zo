package com.trifasico.x50zo.exceptions;

import com.trifasico.x50zo.model.Card;

/**
 * Checked exception thrown when a player attempts to play a card that would
 * cause the table sum to exceed 50, violating the principal game rule.
 *
 * <p>This is a <em>checked</em> exception because the caller (typically the
 * turn manager or the UI controller) is expected to handle it explicitly —
 * for example, by prompting the human player to choose a different card, or
 * by triggering the elimination flow for a machine player.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (currentSum + card.getValue() > 50) {
 *     throw new InvalidPlayException(card, currentSum);
 * }
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see NoPlayableCardException
 */
public class InvalidPlayException extends Exception {

    /** The card that was illegally played. */
    private final Card card;

    /** The table sum at the moment the illegal play was attempted. */
    private final int currentSum;

    /**
     * Constructs an {@code InvalidPlayException} for the given card and current sum.
     *
     * @param card       the card the player tried to play
     * @param currentSum the table sum at the time of the attempted play
     */
    public InvalidPlayException(Card card, int currentSum) {
        super(String.format(
                "Invalid play: card '%s' (value %d) would push the table sum from %d to %d, exceeding 50.",
                card, card.getValue(currentSum), currentSum, currentSum + card.getValue(currentSum)
        ));
        this.card       = card;
        this.currentSum = currentSum;
    }

    /**
     * Returns the card that caused the violation.
     *
     * @return the illegally played {@link Card}
     */
    public Card getCard() {
        return card;
    }

    /**
     * Returns the table sum at the time the exception was thrown.
     *
     * @return the current table sum
     */
    public int getCurrentSum() {
        return currentSum;
    }
}
