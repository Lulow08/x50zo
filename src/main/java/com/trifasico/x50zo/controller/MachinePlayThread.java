package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.TurnManager;

import javafx.application.Platform;

/**
 * A dedicated background thread that processes a machine player's turn with a
 * natural delay before acting, then posts the result back to the JavaFX
 * Application Thread.
 *
 * <h2>Why a dedicated thread instead of a timer</h2>
 * <p>A countdown timer per human turn was considered but rejected: the game
 * rules do not mention a time limit and forcing one creates artificial
 * frustration. The meaningful concurrency requirement is instead fulfilled
 * here — machine decision and delay run off the FX thread so the UI stays
 * responsive (the card-back "thinking" animation can play freely) while the
 * machine waits.</p>
 *
 * <h2>Threading contract</h2>
 * <ol>
 *   <li>{@link #run()} executes on the worker thread: sleeps, then wraps
 *       every model mutation inside {@code Platform.runLater}.</li>
 *   <li>All {@link TurnManager} calls therefore land on the FX thread —
 *       no shared state is touched from two threads simultaneously.</li>
 *   <li>The thread is daemon so it never blocks JVM shutdown.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * MachinePlayThread t = new MachinePlayThread(turnManager, this::onMachineError);
 * t.start();
 * // controller re-enables human input via GameEventListener.onTurnStarted()
 * }</pre>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see TurnManager#machinePlayCard()
 */
public class MachinePlayThread extends Thread {

    /** Milliseconds the thread sleeps before executing the machine's play. */
    public static final long THINKING_MILLIS = 1400L;

    /**
     * Callback interface used to report unexpected errors back to the
     * controller on the FX thread.
     */
    public interface ErrorHandler {
        /**
         * Called on the JavaFX Application Thread when an unrecoverable
         * exception occurs during the machine's turn.
         *
         * @param e the exception that was thrown
         */
        void onError(Exception e);
    }

    private final TurnManager  turnManager;
    private final ErrorHandler errorHandler;

    /**
     * Constructs a {@code MachinePlayThread} as a daemon thread.
     *
     * @param turnManager  the active game session; must not be {@code null}
     * @param errorHandler callback invoked on the FX thread if an unexpected
     *                     error occurs; must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public MachinePlayThread(TurnManager turnManager, ErrorHandler errorHandler) {
        if (turnManager  == null) throw new NullPointerException("turnManager must not be null");
        if (errorHandler == null) throw new NullPointerException("errorHandler must not be null");

        this.turnManager  = turnManager;
        this.errorHandler = errorHandler;

        setDaemon(true);
        setName("machine-play-thread");
    }

    /**
     * Sleeps for {@link #THINKING_MILLIS} ms, then schedules the machine's
     * play on the JavaFX Application Thread via {@code Platform.runLater}.
     *
     * <p>Handles the two expected outcomes:</p>
     * <ul>
     *   <li>{@link NoPlayableCardException} — the machine has no legal move
     *       and is eliminated via {@link TurnManager#eliminateCurrentPlayer()}.</li>
     *   <li>{@link InvalidPlayException} or {@link EmptyDeckException} — these
     *       indicate a strategy bug or an edge-case deck failure; they are
     *       forwarded to the {@link ErrorHandler}.</li>
     * </ul>
     */
    @Override
    public void run() {
        try {
            Thread.sleep(THINKING_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        Platform.runLater(() -> {
            try {
                turnManager.machinePlayCard();
            } catch (NoPlayableCardException e) {
                turnManager.eliminateCurrentPlayer();
            } catch (InvalidPlayException | EmptyDeckException e) {
                errorHandler.onError(e);
            }
        });
    }
}