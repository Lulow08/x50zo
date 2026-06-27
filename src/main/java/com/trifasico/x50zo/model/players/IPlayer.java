package com.trifasico.x50zo.model.players;

import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.Card;

import java.util.List;

/**
 * Defines the complete behavioral contract for any participant in a
 * Cincuentazo game, whether human or machine.
 *
 * <p>This interface is part of the event-driven MVC design: the controller
 * calls these methods during turn resolution and relies on the checked
 * exceptions to branch the game flow (e.g. eliminate a player who cannot
 * move, or reject an illegal card choice from the human).</p>
 *
 * <p>Concrete player types should not implement this interface directly.
 * Instead they should extend {@link PlayerAdapter}, which provides default
 * implementations for hand management, leaving each subclass to override
 * only what is specific to it.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see PlayerAdapter
 */
public interface IPlayer {

    /**
     * Plays the card at the given hand index onto the table.
     *
     * <p>The implementing class must verify that the card is legal before
     * removing it from the hand. If the play is illegal it must throw
     * {@link InvalidPlayException} and leave the hand unchanged.</p>
     *
     * @param cardIndex  zero-based index of the card to play
     * @param currentSum the running table sum before this play
     * @return the {@link Card} that was played and removed from the hand
     * @throws InvalidPlayException    (checked) if the selected card would
     *                                 push the sum above 50
     * @throws IndexOutOfBoundsException if {@code cardIndex} is out of range
     */
    Card playCard(int cardIndex, int currentSum) throws InvalidPlayException;

    /**
     * Adds a card drawn from the deck to this player's hand.
     *
     * @param card the {@link Card} to receive; must not be {@code null}
     */
    void receiveCard(Card card);

    /**
     * Returns an unmodifiable view of the player's current hand.
     *
     * @return the hand as an unmodifiable {@link List}; never {@code null}
     */
    List<Card> getHand();

    /**
     * Returns whether the player has at least one card that can legally be
     * played given the current table sum.
     *
     * @param currentSum the running table sum
     * @return {@code true} if at least one card in hand is playable
     */
    boolean hasPlayableCard(int currentSum);

    /**
     * Checks that the player can move, throwing if they cannot.
     *
     * <p>Convenience method used by the turn manager before attempting
     * {@link #playCard(int, int)}. If the player has no legal move the
     * exception triggers the elimination flow.</p>
     *
     * @param currentSum the running table sum
     * @throws NoPlayableCardException (checked) if no card in hand is playable
     */
    void assertCanPlay(int currentSum) throws NoPlayableCardException;

    /**
     * Returns the player's display name.
     *
     * @return the name; never {@code null} or empty
     */
    String getName();

    /**
     * Returns the number of cards currently in the player's hand.
     *
     * @return hand size (0–4 during normal play)
     */
    int handSize();
}