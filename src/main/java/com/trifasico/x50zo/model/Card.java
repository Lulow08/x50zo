package com.trifasico.x50zo.model;

import java.util.Objects;

/**
 * Represents a single immutable playing card in the Cincuentazo game.
 *
 * <p>A card is defined by its {@link Rank} and its suit. The suit is stored
 * as a plain {@code String} because it carries no game-logic weight — it is
 * used only to identify which image asset the view should render. All value
 * logic is delegated to {@link Rank}.</p>
 *
 * <p>Cards are immutable: once constructed their state never changes, which
 * makes them safe to share across threads without synchronization.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see Rank
 * @see Deck
 */
public record Card(Rank rank, String suit) {

    public static final String SUIT_HEARTS = "hearts";
    public static final String SUIT_DIAMONDS = "diamonds";
    public static final String SUIT_CLUBS = "clubs";
    public static final String SUIT_SPADES = "spades";

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

    /**
     * Returns the effective value of this card given the current table sum.
     *
     * <p>Delegates to {@link Rank#resolveValue(int)}, which handles the
     * Ace's context-sensitive rule (1 or 10).</p>
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

    /**
     * Returns the rank of this card.
     *
     * @return the {@link Rank}
     */
    @Override
    public Rank rank() {
        return rank;
    }

    /**
     * Returns the suit of this card as a lowercase string.
     *
     * <p>The view layer uses this value to build the image asset path,
     * for example {@code "cards/" + suit + "_" + rank.name().toLowerCase() + ".png"}.</p>
     *
     * @return the suit string (e.g. {@code "hearts"})
     */
    @Override
    public String suit() {
        return suit;
    }

    /**
     * Two cards are equal when they share the same rank and suit.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is a {@code Card} with the same rank and suit
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card other = (Card) o;
        return rank == other.rank && suit.equals(other.suit);
    }

    /**
     * Returns a concise log-friendly representation such as {@code "A-hearts"} or
     * {@code "10-spades"}. The view layer uses images, not this string, for display.
     *
     * @return string form of the card
     */
    @Override
    public String toString() {
        return rank.getSymbol() + "-" + suit;
    }
}