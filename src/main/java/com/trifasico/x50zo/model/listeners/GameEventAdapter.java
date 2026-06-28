package com.trifasico.x50zo.model.listeners;

import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.players.IPlayer;

/**
 * No-op adapter for {@link IGameEventListener}.
 *
 * <p>Extend this class and override only the events relevant to a given
 * context. This avoids forcing every implementor to provide empty bodies
 * for all five methods.</p>
 *
 * <p>Typical usage in the controller:</p>
 * <pre>{@code
 * turnManager.setEventListener(new GameEventAdapter() {
 *     @Override
 *     public void onCardPlayed(IPlayer p, Card c, int sum) { renderTopCard(c); }
 *     @Override
 *     public void onGameOver(IPlayer winner) { showEndScreen(winner); }
 * });
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see IGameEventListener
 */
public abstract class GameEventAdapter implements IGameEventListener {

    /** {@inheritDoc} */
    @Override
    public void onCardPlayed(IPlayer player, Card card, int newSum) {}

    /** {@inheritDoc} */
    @Override
    public void onPlayerEliminated(IPlayer eliminated) {}

    /** {@inheritDoc} */
    @Override
    public void onGameOver(IPlayer winner) {}

    /** {@inheritDoc} */
    @Override
    public void onTurnStarted(IPlayer player) {}

    /** {@inheritDoc} */
    @Override
    public void onDeckReshuffled() {}
}