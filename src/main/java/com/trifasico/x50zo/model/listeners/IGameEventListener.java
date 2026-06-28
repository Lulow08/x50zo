package com.trifasico.x50zo.model.listeners;

import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.players.IPlayer;

/**
 * Observer interface for game lifecycle events emitted by
 * {@link com.trifasico.x50zo.model.TurnManager}.
 *
 * <p>The JavaFX controller implements (or extends {@link GameEventAdapter}
 * from) this interface to react to model events without polling. The model
 * knows nothing about JavaFX — it only calls these methods. Background
 * threads must wrap their calls in {@code Platform.runLater} before firing
 * any callback that touches the scene graph.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see GameEventAdapter
 */
public interface IGameEventListener {

    /**
     * Called after any player successfully plays a card.
     *
     * @param player the player who played
     * @param card   the card that was played
     * @param newSum the table sum after the play
     */
    void onCardPlayed(IPlayer player, Card card, int newSum);

    /**
     * Called when a player cannot make a legal move and is eliminated.
     *
     * @param eliminated the player removed from the game
     */
    void onPlayerEliminated(IPlayer eliminated);

    /**
     * Called once when the game ends.
     *
     * @param winner the surviving player, or {@code null} if all were eliminated
     */
    void onGameOver(IPlayer winner);

    /**
     * Called at the start of each turn to signal which player must act next.
     *
     * @param player the player whose turn is beginning
     */
    void onTurnStarted(IPlayer player);

    /**
     * Called when the deck runs out and the table pile is reshuffled into a
     * new draw pile, so the UI can show a brief indicator.
     */
    void onDeckReshuffled();
}