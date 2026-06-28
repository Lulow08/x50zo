package com.trifasico.x50zo.model;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.InvalidPlayerCountException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.listeners.IGameEventListener;
import com.trifasico.x50zo.model.players.HumanPlayer;
import com.trifasico.x50zo.model.players.IPlayer;
import com.trifasico.x50zo.model.players.MachinePlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Central coordinator for a Cincuentazo game session.
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
public class TurnManager {

    public static final int HAND_SIZE = 4;

    private final HumanPlayer         human;
    private final List<MachinePlayer> machines;
    private final Deck                deck;
    private final TablePile           tablePile;
    private final ArrayDeque<IPlayer> turnQueue;

    private boolean            gameOver;
    private IPlayer            winner;
    private IGameEventListener listener;

    public TurnManager(String humanName, int machineCount) {
        if (machineCount < 1 || machineCount > 3)
            throw new InvalidPlayerCountException(machineCount);

        this.human     = new HumanPlayer(humanName);
        this.machines  = new ArrayList<>(machineCount);
        this.deck      = new Deck();
        this.tablePile = new TablePile();
        this.turnQueue = new ArrayDeque<>();
        this.gameOver  = false;
        this.winner    = null;
        this.listener  = null;

        for (int i = 1; i <= machineCount; i++)
            machines.add(new MachinePlayer("Machine " + i));
    }

    public void setEventListener(IGameEventListener listener) {
        this.listener = listener;
    }

    public void startGame() throws EmptyDeckException {
        turnQueue.clear();
        turnQueue.addLast(human);
        for (MachinePlayer m : machines) turnQueue.addLast(m);

        for (IPlayer player : turnQueue)
            for (int i = 0; i < HAND_SIZE; i++)
                player.receiveCard(drawCard());

        tablePile.push(drawCard());
        fire(l -> l.onTurnStarted(currentPlayer()));
    }

    public IPlayer currentPlayer() {
        if (turnQueue.isEmpty()) throw new IllegalStateException("No active players in queue.");
        return turnQueue.peekFirst();
    }

    public Card humanPlayCard(int cardIndex) throws InvalidPlayException, EmptyDeckException {
        Card played = human.playSelectedCard(cardIndex, tablePile.getCurrentSum());
        tablePile.push(played);
        int newSum = tablePile.getCurrentSum();
        replenishHand(human);
        fire(l -> l.onCardPlayed(human, played, newSum));
        advanceTurn();
        return played;
    }

    public Card machinePlayCard()
            throws NoPlayableCardException, InvalidPlayException, EmptyDeckException {

        IPlayer current = currentPlayer();
        if (!(current instanceof MachinePlayer machine)) {
            throw new IllegalStateException("Current player is not a MachinePlayer.");
        }

        Card played = machine.decideAndPlay(buildGameState());
        tablePile.push(played);
        int newSum = tablePile.getCurrentSum();
        replenishHand(machine);
        fire(l -> l.onCardPlayed(machine, played, newSum));
        advanceTurn();
        return played;
    }

    public void eliminateCurrentPlayer() {
        IPlayer eliminated = turnQueue.pollFirst();
        if (eliminated == null) return;

        for (Card card : eliminated.getHand())
            deck.addToBottom(card);

        fire(l -> l.onPlayerEliminated(eliminated));

        if (checkWinCondition()) return;
        if (!turnQueue.isEmpty())
            fire(l -> l.onTurnStarted(currentPlayer()));
    }

    public boolean isGameOver()            { return gameOver; }
    public IPlayer getWinner()             { return winner; }
    public HumanPlayer getHuman()          { return human; }
    public int getTableSum()               { return tablePile.getCurrentSum(); }
    public Card getTopCard()               { return tablePile.peek(); }
    public int remainingDeckCards()        { return deck.remainingCards(); }

    public List<IPlayer> getActivePlayers() {
        return List.copyOf(turnQueue);
    }

    private void advanceTurn() {
        if (turnQueue.size() > 1)
            turnQueue.addLast(turnQueue.pollFirst());

        if (checkWinCondition()) return;
        if (!turnQueue.isEmpty())
            fire(l -> l.onTurnStarted(currentPlayer()));
    }

    private void replenishHand(IPlayer player) throws EmptyDeckException {
        if (player.handSize() < HAND_SIZE)
            player.receiveCard(drawCard());
    }

    private Card drawCard() throws EmptyDeckException {
        if (deck.isEmpty()) {
            List<Card> pile = tablePile.collectForReshuffle();
            deck.reshuffleFromPile(pile);
            fire(IGameEventListener::onDeckReshuffled);
        }
        return deck.draw();
    }

    /** @return true if game ended */
    private boolean checkWinCondition() {
        if (turnQueue.size() == 1) {
            gameOver = true;
            winner   = turnQueue.peekFirst();
            fire(l -> l.onGameOver(winner));
            return true;
        } else if (turnQueue.isEmpty()) {
            gameOver = true;
            fire(l -> l.onGameOver(null));
            return true;
        }
        return false;
    }

    private GameState buildGameState() {
        return new GameState(tablePile.getCurrentSum(), deck.remainingCards(), turnQueue.size());
    }

    @FunctionalInterface
    private interface ListenerAction {
        void call(IGameEventListener l);
    }

    private void fire(ListenerAction action) {
        if (listener != null) action.call(listener);
    }
}