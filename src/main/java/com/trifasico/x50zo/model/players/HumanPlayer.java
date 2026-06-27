package com.trifasico.x50zo.model.players;

import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.model.Card;

import java.util.List;

/**
 * Represents the human participant in a Cincuentazo game.
 *
 * <p>Extends {@link PlayerAdapter} and relies entirely on its hand-management
 * implementation. The only responsibility this class adds is the
 * <em>selection strategy</em>: the human picks a card by index, supplied
 * externally by the UI layer (the controller calls
 * {@link #playSelectedCard(int, int)} after the player taps/clicks a card).</p>
 *
 * <p>Design notes:</p>
 * <ul>
 *   <li>No AI logic lives here — card choice is always driven by the view.</li>
 *   <li>The class is intentionally thin; any state that belongs to all players
 *       (hand, name, playability checks) is in {@link PlayerAdapter}.</li>
 *   <li>The controller should call {@link #hasPlayableCard(int)} before
 *       enabling the "play" button so the user is never offered an illegal move.</li>
 * </ul>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see PlayerAdapter
 * @see MachinePlayer
 */

public class HumanPlayer extends PlayerAdapter {
    /**
     * Constructs a {@code HumanPlayer} with the given display name.
     *
     * @param name the human player's display name; must not be {@code null} or blank
     * @throws NullPointerException     if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Plays the card at {@code cardIndex} from the human's hand.
     *
     * <p>This is the primary entry point called by the controller when the
     * human selects a card in the UI. Delegates validation and removal to
     * {@link PlayerAdapter#playCard(int, int)}, which throws
     * {@link InvalidPlayException} if the card would push the table sum above 50.</p>
     *
     * <p>Typical controller usage:</p>
     * <pre>{@code
     * try {
     *     Card played = humanPlayer.playSelectedCard(selectedIndex, tableSum);
     *     tableSum += played.getValue(tableSum);
     * } catch (InvalidPlayException e) {
     *     // show error feedback in the UI
     * }
     * }</pre>
     *
     * @param cardIndex  zero-based index of the card the human chose
     * @param currentSum the running table sum before this play
     * @return the {@link Card} that was played and removed from the hand
     * @throws InvalidPlayException      if the selected card would exceed the sum of 50
     * @throws IndexOutOfBoundsException if {@code cardIndex} is outside [0, handSize)
     */
    public Card playSelectedCard(int cardIndex, int currentSum) throws InvalidPlayException {
        return playCard(cardIndex, currentSum);
    }

    /**
     * Returns the indices of all cards in the human's hand that are legally
     * playable given {@code currentSum}.
     *
     * <p>The controller can use this list to highlight (or enable) only the
     * cards the player may actually pick, providing visual feedback before a
     * selection is made.</p>
     *
     * @param currentSum the running table sum
     * @return a list of zero-based indices of playable cards; empty if none
     */
    public List<Integer> getPlayableIndices(int currentSum) {
        List<Card> hand = getHand();
        List<Integer> playable = new java.util.ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isPlayable(currentSum)) {
                playable.add(i);
            }
        }
        return java.util.Collections.unmodifiableList(playable);
    }

}
