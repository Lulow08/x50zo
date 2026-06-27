package com.trifasico.x50zo.model;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.InvalidPlayerCountException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.players.HumanPlayer;
import com.trifasico.x50zo.model.players.IPlayer;
import com.trifasico.x50zo.model.players.MachinePlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Central coordinator for a Cincuentazo game session.
 *
 * <p>{@code TurnManager} owns the authoritative game state: the active player
 * queue, the deck, and the table pile. The JavaFX controller holds a reference
 * to this class and calls its methods in response to user actions or background
 * thread events. No game logic lives in the controller.</p>
 *
 * <h2>Turn lifecycle</h2>
 * <ol>
 *   <li>Controller calls {@link #startGame()} once during setup.</li>
 *   <li>Controller queries {@link #currentPlayer()} to know whose turn it is.</li>
 *   <li>For a {@link HumanPlayer}: the UI waits for input, then calls
 *       {@link #humanPlayCard(int)}.</li>
 *   <li>For a {@link MachinePlayer}: {@code MachineThinkingTask} (a JavaFX
 *       {@code Task}) runs on a background thread, calls
 *       {@link #machinePlayCard()}, and posts the result to the UI thread.</li>
 *   <li>After every play, the manager draws a replacement card, checks for
 *       elimination, checks for a winner, and advances the turn.</li>
 * </ol>
 *
 * <h2>Player queue</h2>
 * <p>Active players are stored in an {@link ArrayDeque} used as a circular
 * queue: after each turn the current player is moved to the tail, so
 * {@code peekFirst()} always returns the player whose turn it is. Eliminated
 * players are simply never re-added.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see TablePile
 * @see Deck
 */
public class TurnManager {

    /** Required hand size at all times during play. */
    public static final int HAND_SIZE = 4;

    private final HumanPlayer          human;
    private final List<MachinePlayer>  machines;
    private final Deck                 deck;
    private final TablePile            tablePile;
    private final ArrayDeque<IPlayer>  turnQueue;

    private boolean gameOver;
    private IPlayer winner;

    /**
     * Constructs a {@code TurnManager} and validates the player count.
     *
     * @param humanName    the human player's display name
     * @param machineCount number of machine opponents (1–3)
     * @throws InvalidPlayerCountException (unchecked) if {@code machineCount}
     *                                     is outside [1, 3]
     */
    public TurnManager(String humanName, int machineCount) {
        if (machineCount < 1 || machineCount > 3) {
            throw new InvalidPlayerCountException(machineCount);
        }

        this.human     = new HumanPlayer(humanName);
        this.machines  = new ArrayList<>(machineCount);
        this.deck      = new Deck();
        this.tablePile = new TablePile();
        this.turnQueue = new ArrayDeque<>();
        this.gameOver  = false;
        this.winner    = null;

        for (int i = 1; i <= machineCount; i++) {
            machines.add(new MachinePlayer("Machine " + i));
        }
    }

    /**
     * Deals four cards to every player, places the opening card on the table,
     * and populates the turn queue (human always goes first).
     *
     * <p>Must be called exactly once before any turn methods.</p>
     *
     * @throws EmptyDeckException (checked) if the deck runs out during initial
     *                            dealing — practically impossible with 52 cards
     *                            and at most 4 players, but declared for safety
     */
    public void startGame() throws EmptyDeckException {
        turnQueue.clear();
        turnQueue.addLast(human);
        for (MachinePlayer m : machines) turnQueue.addLast(m);

        for (IPlayer player : turnQueue) {
            for (int i = 0; i < HAND_SIZE; i++) {
                player.receiveCard(drawCard());
            }
        }

        tablePile.push(drawCard());
    }

    /**
     * Returns the player whose turn it is without advancing the queue.
     *
     * @return the current {@link IPlayer}
     * @throws IllegalStateException if the game has not started or is already over
     */
    public IPlayer currentPlayer() {
        if (turnQueue.isEmpty()) throw new IllegalStateException("No active players in queue.");
        return turnQueue.peekFirst();
    }

    /**
     * Executes the human player's turn by playing the card at {@code cardIndex}.
     *
     * <p>Called by the controller after the human clicks a card. If the card
     * is illegal, the exception propagates to the controller so the UI can
     * show feedback without advancing the turn.</p>
     *
     * @param cardIndex zero-based index of the card the human selected
     * @return the {@link Card} that was played
     * @throws InvalidPlayException (checked) if the card would exceed the sum of 50
     * @throws EmptyDeckException   (checked) if drawing a replacement card fails
     */
    public Card humanPlayCard(int cardIndex) throws InvalidPlayException, EmptyDeckException {
        Card played = human.playSelectedCard(cardIndex, tablePile.getCurrentSum());
        tablePile.push(played);
        replenishHand(human);
        advanceTurn();
        return played;
    }

    /**
     * Executes the current machine player's turn using its built-in strategy.
     *
     * <p>Called from {@code MachineThinkingTask} on a background thread.
     * This method is the only one accessed off the JavaFX thread; all fields
     * it touches ({@code tablePile}, {@code deck}) are not accessed concurrently
     * because the task runs while the UI is locked for the machine's turn.</p>
     *
     * @return the {@link Card} the machine played
     * @throws NoPlayableCardException (checked) if the machine has no legal move
     *                                 and must be eliminated
     * @throws InvalidPlayException    (checked) if the strategy chose an
     *                                 unplayable card — indicates a strategy bug
     * @throws EmptyDeckException      (checked) if drawing a replacement card fails
     * @throws IllegalStateException   if the current player is not a machine
     */
    public Card machinePlayCard()
            throws NoPlayableCardException, InvalidPlayException, EmptyDeckException {

        IPlayer current = currentPlayer();
        if (!(current instanceof MachinePlayer)) {
            throw new IllegalStateException("Current player is not a MachinePlayer.");
        }
        MachinePlayer machine = (MachinePlayer) current;

        GameState state = buildGameState();
        Card played = machine.decideAndPlay(state);
        tablePile.push(played);
        replenishHand(machine);
        advanceTurn();
        return played;
    }

    /**
     * Eliminates the current player (they could not make a legal move), sends
     * their hand to the deck, and advances the turn.
     *
     * <p>Called by the controller when {@link #machinePlayCard()} throws
     * {@link NoPlayableCardException}, or when the human timer expires.</p>
     */
    public void eliminateCurrentPlayer() {
        IPlayer eliminated = turnQueue.pollFirst();
        if (eliminated == null) return;

        for (Card card : eliminated.getHand()) {
            deck.addToBottom(card);
        }

        checkWinCondition();
    }

    /**
     * Returns {@code true} if the game has ended.
     *
     * @return game-over flag
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the winner of the game, or {@code null} if the game is still
     * in progress.
     *
     * @return the winning {@link IPlayer}, or {@code null}
     */
    public IPlayer getWinner() {
        return winner;
    }

    /**
     * Returns an unmodifiable snapshot of the players still active, in turn order.
     *
     * @return active players list
     */
    public List<IPlayer> getActivePlayers() {
        return List.copyOf(turnQueue);
    }

    /**
     * Returns the human player.
     *
     * @return the {@link HumanPlayer}
     */
    public HumanPlayer getHuman() {
        return human;
    }

    /**
     * Returns the current table sum.
     *
     * @return table sum
     */
    public int getTableSum() {
        return tablePile.getCurrentSum();
    }

    /**
     * Returns the top card of the table pile (the last card played).
     *
     * @return the top {@link Card}
     */
    public Card getTopCard() {
        return tablePile.peek();
    }

    /**
     * Returns the number of cards remaining in the deck.
     *
     * @return remaining deck size
     */
    public int remainingDeckCards() {
        return deck.remainingCards();
    }

    private void advanceTurn() {
        if (turnQueue.size() > 1) {
            turnQueue.addLast(turnQueue.pollFirst());
        }
        checkWinCondition();
    }

    private void replenishHand(IPlayer player) throws EmptyDeckException {
        if (player.handSize() < HAND_SIZE) {
            player.receiveCard(drawCard());
        }
    }

    private Card drawCard() throws EmptyDeckException {
        if (deck.isEmpty()) {
            List<Card> pile = tablePile.collectForReshuffle();
            deck.reshuffleFromPile(pile);
        }
        return deck.draw();
    }

    private void checkWinCondition() {
        if (turnQueue.size() == 1) {
            gameOver = true;
            winner   = turnQueue.peekFirst();
        } else if (turnQueue.isEmpty()) {
            gameOver = true;
        }
    }

    private GameState buildGameState() {
        return new GameState(
                tablePile.getCurrentSum(),
                deck.remainingCards(),
                turnQueue.size()
        );
    }
}