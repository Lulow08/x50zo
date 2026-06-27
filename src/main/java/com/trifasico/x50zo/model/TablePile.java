package com.trifasico.x50zo.model;

import com.trifasico.x50zo.exceptions.EmptyDeckException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the central pile of played cards on the table in a Cincuentazo game.
 *
 * <p>Backed by an {@link ArrayDeque}, the third of the three required non-array
 * data structures. {@code ArrayDeque} is the right choice here because the pile
 * needs O(1) push to the top ({@code addFirst}) and O(1) peek of the top card
 * (the one shown face-up on the table), while also allowing full iteration when
 * the pile must be collected and reshuffled into the deck.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Hold every card played so far, with the most recent on top.</li>
 *   <li>Track the running table sum, updated on each push.</li>
 *   <li>Supply all cards except the top one when the deck needs a reshuffle.</li>
 * </ul>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see Deck
 */
public class TablePile {

    private final ArrayDeque<Card> pile;
    private int currentSum;

    /**
     * Constructs an empty {@code TablePile} with a sum of zero.
     * The first card is pushed via {@link #push(Card)}.
     */
    public TablePile() {
        this.pile       = new ArrayDeque<>();
        this.currentSum = 0;
    }

    /**
     * Places a card on top of the pile and updates the running sum.
     *
     * <p>The card's value is resolved against the sum <em>before</em> the
     * push so that the Ace rule (1 or 10) uses the correct context.</p>
     *
     * @param card the card to add; must not be {@code null}
     * @throws NullPointerException if {@code card} is {@code null}
     */
    public void push(Card card) {
        if (card == null) throw new NullPointerException("card must not be null");
        currentSum += card.getValue(currentSum);
        pile.addFirst(card);
    }

    /**
     * Returns the top card of the pile (the last card played) without
     * removing it. This is the card shown face-up on the table.
     *
     * @return the top {@link Card}
     * @throws IllegalStateException if the pile is empty
     */
    public Card peek() {
        if (pile.isEmpty()) throw new IllegalStateException("TablePile is empty.");
        return pile.peekFirst();
    }

    /**
     * Returns the current running sum of all cards played so far.
     *
     * @return the table sum
     */
    public int getCurrentSum() {
        return currentSum;
    }

    /**
     * Returns the number of cards currently in the pile.
     *
     * @return pile size
     */
    public int size() {
        return pile.size();
    }

    /**
     * Returns {@code true} if no card has been played yet.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return pile.isEmpty();
    }

    /**
     * Collects all cards from the pile <em>except</em> the top one, clears
     * them from the pile, and returns them so the {@link Deck} can reshuffle
     * them. The top card and the current sum are preserved.
     *
     * <p>Called by the game engine when the deck runs out of cards, per the
     * game rules: "take the table cards except the last played, shuffle them,
     * and leave them available in the deck."</p>
     *
     * @return a mutable list of cards available for reshuffling; empty if the
     *         pile has one card or fewer
     * @throws EmptyDeckException if there are not enough pile cards to form a
     *                            new deck (pile has 0 or 1 cards total)
     */
    public List<Card> collectForReshuffle() throws EmptyDeckException {
        if (pile.size() <= 1) {
            throw new EmptyDeckException(pile.size());
        }

        Card topCard = pile.removeFirst();

        List<Card> collected = new ArrayList<>(pile);
        pile.clear();

        pile.addFirst(topCard);

        return collected;
    }
}