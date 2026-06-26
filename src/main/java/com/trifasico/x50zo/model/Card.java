package com.trifasico.x50zo.model;

import java.util.Objects;

/**
 * Represents a single immutable playing card in the Cincuentazo game.
 *
 * <p>A card is defined by its {@link Rank} and its suit. The suit is stored
 * as a plain {@code String} (e.g. {@code "hearts"}) because it carries no
 * game-logic weight — it is used only for sprite selection in the view layer.
 * All value logic is delegated to {@link Rank}.</p>
 *
 * <p>Cards are immutable: once constructed their state never changes, which
 * makes them safe to share across threads without synchronisation.</p>
 *
 * @author Lulow
 * @version 1.0
 * @see Rank
 * @see Deck
 */
public final class Card {

    // -------------------------------------------------------------------------
    // Valid suit constants — use these instead of raw strings
    // -------------------------------------------------------------------------

    public static final String SUIT_HEARTS   = "hearts";
    public static final String SUIT_DIAMONDS = "diamonds";
    public static final String SUIT_CLUBS    = "clubs";
    public static final String SUIT_SPADES   = "spades";

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Rank   rank;
    private final String suit;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code Card} with the specified rank and suit.
     *
     * @param rank the rank of the card; must not be {@code null}
     * @param suit the suit of the card (use the {@code SUIT_*} constants);
     *             must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public Card(Rank rank, String suit) {
        this.rank = Objects.requireNonNull(rank, "rank must not be null");
        this.suit = Objects.requireNonNull(suit, "suit must not be null");
    }

    // -------------------------------------------------------------------------
    // Game-logic API
    // -------------------------------------------------------------------------

    /**
     * Returns the effective value of this card given the current table sum.
     *
     * <p>Delegates to {@link Rank#resolveValue(int)}, which handles the
     * Ace's context-sensitive rule.</p>
     *
     * @param currentSum the running table sum before this card is played
     * @return the value to apply to the table sum
     */
    public int getValue(int currentSum) {
        return rank.resolveValue(currentSum);
    }

    /**
     * Returns whether this card can legally be played given the current
     * table sum, i.e. playing it would not push the sum above 50.
     *
     * @param currentSum the running table sum before this card is played
     * @return {@code true} if this card is a legal play
     */
    public boolean isPlayable(int currentSum) {
        return rank.isPlayable(currentSum);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the rank of this card.
     *
     * @return the {@link Rank}
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns the suit of this card as a lowercase string.
     *
     * @return the suit (e.g. {@code "hearts"})
     */
    public String getSuit() {
        return suit;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Two cards are equal when they share the same rank and suit.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is a {@code Card} with the same
     *         rank and suit
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card other)) return false;
        return rank == other.rank && suit.equals(other.suit);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    /**
     * Returns a concise representation such as {@code "A♠"} or {@code "10♥"}.
     *
     * @return string form of the card
     */
    @Override
    public String toString() {
        return rank.getSymbol() + suitSymbol();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Maps the suit string to a Unicode suit symbol for compact display. */
    private String suitSymbol() {
        return switch (suit) {
            case SUIT_HEARTS   -> "♥";
            case SUIT_DIAMONDS -> "♦";
            case SUIT_CLUBS    -> "♣";
            case SUIT_SPADES   -> "♠";
            default            -> suit;   // fallback: just print the raw string
        };
    }
}