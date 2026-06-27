package com.trifasico.x50zo.model.players;

import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Adapter class that provides a complete default implementation of
 * {@link IPlayer} for any Cincuentazo player.
 *
 * <p>Subclasses ({@code HumanPlayer}, {@code MachinePlayer}) extend this
 * class instead of implementing {@link IPlayer} directly, so they only
 * need to override the behavior that is specific to them. This follows the
 * <em>adapter</em> pattern: the interface defines the full contract, and
 * this abstract class absorbs every method that is identical for all players.</p>
 *
 * <p>The hand is backed by an {@link ArrayList}, the second of the three
 * required non-array data structures. A fixed-capacity list is appropriate
 * here because the hand size is bounded (always 0–4 cards) and random-index
 * access is needed when the human selects a card by position.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see IPlayer
 */
public abstract class PlayerAdapter implements IPlayer {

    /** Maximum number of cards a player may hold at any time. */
    public static final int MAX_HAND_SIZE = 4;

    private final String     name;
    private final List<Card> hand;

    /**
     * Constructs an {@code PlayerAdapter} with the given name and an empty hand.
     *
     * @param name the display name of this player; must not be {@code null} or blank
     * @throws NullPointerException     if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank
     */
    protected PlayerAdapter(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        this.name = name;
        this.hand = new ArrayList<>(MAX_HAND_SIZE);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifies the play is legal before removing the card from the hand.
     * The hand is not modified if the play is rejected.</p>
     *
     * @throws InvalidPlayException      if the card would push the sum above 50
     * @throws IndexOutOfBoundsException if {@code cardIndex} is out of range
     */
    @Override
    public Card playCard(int cardIndex, int currentSum) throws InvalidPlayException {
        Card chosen = hand.get(cardIndex);
        if (!chosen.isPlayable(currentSum)) {
            throw new InvalidPlayException(chosen, currentSum);
        }
        hand.remove(cardIndex);
        return chosen;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the hand is already at {@link #MAX_HAND_SIZE}
     */
    @Override
    public void receiveCard(Card card) {
        Objects.requireNonNull(card, "card must not be null");
        if (hand.size() >= MAX_HAND_SIZE) {
            throw new IllegalStateException(
                    name + "'s hand is already full (" + MAX_HAND_SIZE + " cards)."
            );
        }
        hand.add(card);
    }

    /**
     * {@inheritDoc}
     *
     * @return an unmodifiable view of the hand; the underlying list is
     *         modified by {@link #playCard} and {@link #receiveCard}
     */
    @Override
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPlayableCard(int currentSum) {
        for (Card card : hand) {
            if (card.isPlayable(currentSum)) return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void assertCanPlay(int currentSum) throws NoPlayableCardException {
        if (!hasPlayableCard(currentSum)) {
            throw new NoPlayableCardException(name, currentSum);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int handSize() {
        return hand.size();
    }

    /**
     * Returns a log-friendly summary of this player's current hand.
     *
     * @return string such as {@code "Player1 [A-hearts, K-spades, 7-clubs, 2-diamonds]"}
     */
    @Override
    public String toString() {
        return name + " " + hand;
    }
}