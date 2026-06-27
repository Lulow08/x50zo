package com.trifasico.x50zo.model.players;

import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.GameState;

/**
 * Represents a machine-controlled participant in a Cincuentazo game.
 *
 * <p>Extends {@link PlayerAdapter} for hand management and delegates all
 * card-selection decisions to {@link MachineStrategy}. This separation means
 * the machine player itself contains no decision logic — it only orchestrates
 * the call to the strategy and then executes the resulting play through the
 * inherited {@link #playCard(int, int)} method.</p>
 *
 * <p>The actual delay that simulates "thinking" is not the responsibility of
 * this class. It belongs to {@code MachineThinkingTask} in the controller
 * layer, which runs on a background thread and calls
 * {@link #decideAndPlay(GameState)} once the delay has elapsed.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see PlayerAdapter
 * @see MachineStrategy
 */
public class MachinePlayer extends PlayerAdapter {

    private final MachineStrategy strategy;

    /**
     * Constructs a {@code MachinePlayer} with the given name and a default
     * {@link MachineStrategy} instance.
     *
     * @param name the display name (e.g. {@code "Machine 1"}); must not be
     *             {@code null} or blank
     */
    public MachinePlayer(String name) {
        this(name, new MachineStrategy());
    }

    /**
     * Constructs a {@code MachinePlayer} with the given name and a specific
     * strategy instance, useful for injecting a pre-configured strategy in tests.
     *
     * @param name     the display name; must not be {@code null} or blank
     * @param strategy the card-selection strategy; must not be {@code null}
     * @throws NullPointerException if {@code strategy} is {@code null}
     */
    public MachinePlayer(String name, MachineStrategy strategy) {
        super(name);
        if (strategy == null) throw new NullPointerException("strategy must not be null");
        this.strategy = strategy;
    }

    /**
     * Asks the strategy to select a card and immediately plays it.
     *
     * <p>This is the single entry point the turn manager calls on the machine's
     * turn. The method:</p>
     * <ol>
     *   <li>Verifies the machine has at least one playable card, throwing
     *       {@link NoPlayableCardException} if it does not (triggering
     *       elimination in the caller).</li>
     *   <li>Delegates index selection to {@link MachineStrategy}.</li>
     *   <li>Executes the play via the inherited {@link #playCard(int, int)},
     *       which removes the card from the hand and returns it.</li>
     * </ol>
     *
     * @param gameState a snapshot of the current game context for the strategy
     * @return the {@link Card} the machine played
     * @throws NoPlayableCardException (checked) if no card in hand can be
     *                                 played without exceeding 50
     * @throws InvalidPlayException    (checked) if the strategy returned an
     *                                 index whose card is not actually playable —
     *                                 this indicates a bug in the strategy
     */
    public Card decideAndPlay(GameState gameState)
            throws NoPlayableCardException, InvalidPlayException {

        int tableSum = gameState.tableSum();
        assertCanPlay(tableSum);

        int chosenIndex = strategy.chooseCardIndex(getHand(), gameState);
        return playCard(chosenIndex, tableSum);
    }
}