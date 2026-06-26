package com.trifasico.x50zo.model;

import com.trifasico.x50zo.exceptions.EmptyDeckException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the draw pile (deck) of cards in a Cincuentazo game.
 *
 * <p>Internally backed by a {@link LinkedList} — the first of the three
 * required non-array data structures — because the deck is used exclusively
 * as a queue: cards are added to the tail (reshuffle) and drawn from the
 * head, making {@code LinkedList} the natural fit over {@code ArrayList}.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Build and hold a standard 52-card deck.</li>
 *   <li>Shuffle itself into a random order.</li>
 *   <li>Deal one card at a time from the top.</li>
 *   <li>Accept cards from a table pile and reshuffle them into a new deck
 *       when the draw pile runs out.</li>
 * </ul>
 *
 * @author Lulow
 * @version 1.0
 * @see Card
 * @see EmptyDeckException
 */
public class Deck {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Internal draw pile. Head = top of deck (next card to be drawn). */
    private final LinkedList<Card> cards;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a full, shuffled 52-card deck ready for dealing.
     */
    public Deck() {
        cards = new LinkedList<>();
        build();
        shuffle();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Draws the top card from the deck.
     *
     * <p>If the deck is empty this method throws {@link EmptyDeckException}.
     * The caller should first try to reshuffle the table pile via
     * {@link #reshuffleFromPile(List)} before drawing.</p>
     *
     * @return the top {@link Card}
     * @throws EmptyDeckException (checked) if the deck is empty
     */
    public Card draw() throws EmptyDeckException {
        if (cards.isEmpty()) {
            throw new EmptyDeckException(0);
        }
        return cards.removeFirst();
    }

    /**
     * Takes the given list of cards (the table pile minus its top card),
     * shuffles them, and loads them as the new draw pile.
     *
     * <p>Called automatically by the game engine when the deck runs out.
     * The table sum is <em>not</em> affected by this operation.</p>
     *
     * @param pileCards the cards to reshuffle; must contain at least one card
     * @throws EmptyDeckException (checked) if {@code pileCards} is empty
     */
    public void reshuffleFromPile(List<Card> pileCards) throws EmptyDeckException {
        if (pileCards == null || pileCards.isEmpty()) {
            throw new EmptyDeckException(0);
        }

        List<Card> toShuffle = new ArrayList<>(pileCards);
        Collections.shuffle(toShuffle);

        cards.clear();
        cards.addAll(toShuffle);
    }

    /**
     * Returns {@code true} if there are no cards left in the deck.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns the number of cards currently remaining in the deck.
     *
     * @return remaining card count (0 or more)
     */
    public int remainingCards() {
        return cards.size();
    }

    /**
     * Shuffles the deck into a new random order without changing its contents.
     * Can be called at any time (e.g. for testing with a known seed via
     * a seeded {@link java.util.Random} if needed in future).
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Populates the deck with all 52 cards (13 ranks × 4 suits).
     * Called only during construction.
     */
    private void build() {
        String[] suits = {
                Card.SUIT_HEARTS,
                Card.SUIT_DIAMONDS,
                Card.SUIT_CLUBS,
                Card.SUIT_SPADES
        };

        for (String suit : suits) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }
}