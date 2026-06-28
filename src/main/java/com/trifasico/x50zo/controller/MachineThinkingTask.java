package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.NoPlayableCardException;
import com.trifasico.x50zo.model.TurnManager;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * A JavaFX {@link Task} that simulates a machine player "thinking" before
 * acting, running the decision and play logic on a background thread.
 *
 * <h2>Threading model</h2>
 * <p>JavaFX requires that all scene-graph mutations happen on the Application
 * Thread. {@code Task} runs its {@link #call()} method on a worker thread
 * managed by a {@link java.util.concurrent.ExecutorService} or a plain
 * {@link Thread}. This class therefore:</p>
 * <ol>
 *   <li>Sleeps for {@link #THINKING_MILLIS} ms on the worker thread to
 *       create the illusion of deliberation.</li>
 *   <li>Wraps the actual play inside {@code Platform.runLater} so that
 *       {@link TurnManager#machinePlayCard()} — which mutates model state
 *       and fires {@code GameEventListener} callbacks that update the UI —
 *       executes on the Application Thread.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * MachineThinkingTask task = new MachineThinkingTask(turnManager);
 * task.setOnFailed(e -> handleError(task.getException()));
 * new Thread(task).start();
 * }</pre>
 *
 * <p>Errors that occur inside {@code Platform.runLater} cannot be caught by
 * {@code setOnFailed}; they are instead forwarded to
 * {@link #onPlayError(Exception)} which subclasses or lambdas can override.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see TurnManager#machinePlayCard()
 */
public class MachineThinkingTask extends Task<Void> {

    /** Milliseconds the task sleeps before executing the machine's play. */
    public static final long THINKING_MILLIS = 1500L;

    private final TurnManager turnManager;

    /**
     * Constructs a {@code MachineThinkingTask} for the given game session.
     *
     * @param turnManager the active game coordinator; must not be {@code null}
     * @throws NullPointerException if {@code turnManager} is {@code null}
     */
    public MachineThinkingTask(TurnManager turnManager) {
        if (turnManager == null) throw new NullPointerException("turnManager must not be null");
        this.turnManager = turnManager;
    }

    /**
     * Sleeps for {@link #THINKING_MILLIS} ms, then schedules the machine's
     * play on the JavaFX Application Thread via {@code Platform.runLater}.
     *
     * <p>This method runs on a worker thread. It returns {@code null} (Void)
     * because its result is communicated through the {@link TurnManager}'s
     * event listener rather than through the task's value.</p>
     *
     * @return {@code null}
     * @throws InterruptedException if the sleep is interrupted, which marks
     *                              the task as failed via JavaFX Task machinery
     */
    @Override
    protected Void call() throws InterruptedException {
        Thread.sleep(THINKING_MILLIS);

        Platform.runLater(() -> {
            try {
                turnManager.machinePlayCard();
            } catch (NoPlayableCardException e) {
                turnManager.eliminateCurrentPlayer();
            } catch (InvalidPlayException | EmptyDeckException e) {
                onPlayError(e);
            }
        });

        return null;
    }

    /**
     * Called on the JavaFX Application Thread when an unexpected checked
     * exception occurs during {@link TurnManager#machinePlayCard()}.
     *
     * <p>The default implementation re-throws the exception wrapped in a
     * {@link RuntimeException} so the error surfaces visibly during
     * development. Override this method in the controller to show a dialog
     * or gracefully end the game instead.</p>
     *
     * @param e the exception thrown by the turn manager
     */
    protected void onPlayError(Exception e) {
        throw new RuntimeException("Unexpected error during machine turn: " + e.getMessage(), e);
    }
}