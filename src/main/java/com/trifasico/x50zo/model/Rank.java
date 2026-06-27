package com.trifasico.x50zo.model;

/**
 * Represents the rank of a playing card and owns its value logic
 * for the Cincuentazo game.
 *
 * <p>Value rules:</p>
 * <ul>
 *   <li>TWO through EIGHT and TEN → face value</li>
 *   <li>NINE → 0 (neither adds nor subtracts)</li>
 *   <li>JACK, QUEEN, KING → −10</li>
 *   <li>ACE → 1 or 10, whichever keeps the table sum at or below 50</li>
 * </ul>
 *
 * <p>The {@link #name()} of each constant (e.g. {@code "HEARTS"}, {@code "TWO"})
 * is also used by the view layer to build image-asset paths, so constant names
 * must not be renamed without updating the resource naming convention.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
public enum Rank {

    TWO   ("2",   2),
    THREE ("3",   3),
    FOUR  ("4",   4),
    FIVE  ("5",   5),
    SIX   ("6",   6),
    SEVEN ("7",   7),
    EIGHT ("8",   8),
    NINE  ("9",   0),
    TEN   ("10", 10),
    JACK  ("J",  -10),
    QUEEN ("Q",  -10),
    KING  ("K",  -10),
    ACE   ("A",   0);

    /**
     * Short display symbol used in logs (e.g. {@code "J"}, {@code "10"}, {@code "A"}).
     * The view uses image assets, not this symbol, for on-screen rendering.
     */
    private final String symbol;

    /**
     * Base value. For {@link #ACE} this is always 0 — callers must use
     * {@link #resolveValue(int)} to obtain the context-aware value.
     */
    private final int baseValue;

    Rank(String symbol, int baseValue) {
        this.symbol    = symbol;
        this.baseValue = baseValue;
    }

    /**
     * Returns the short display symbol for this rank (e.g. {@code "J"}, {@code "10"}).
     *
     * @return the rank symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the effective value of this rank given the current table sum.
     *
     * <p>For all ranks except {@link #ACE} the result is the constant base value.
     * For {@link #ACE} the method selects 10 if adding 10 keeps the sum at or
     * below 50; otherwise it selects 1.</p>
     *
     * @param currentSum the running table sum before the card is played
     * @return the value to add to (or subtract from) the table sum
     */
    public int resolveValue(int currentSum) {
        if (this == ACE) {
            return (currentSum + 10 <= 50) ? 10 : 1;
        }
        return baseValue;
    }

    /**
     * Returns whether a card of this rank can legally be played given the
     * current table sum, i.e. the resulting sum would not exceed 50.
     *
     * @param currentSum the running table sum before the card is played
     * @return {@code true} if the play is legal
     */
    public boolean isPlayable(int currentSum) {
        return currentSum + resolveValue(currentSum) <= 50;
    }

    /**
     * {@inheritDoc}
     *
     * @return the rank symbol (e.g. {@code "K"}, {@code "7"})
     */
    @Override
    public String toString() {
        return symbol;
    }
}