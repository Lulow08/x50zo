package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.TurnManager;
import com.trifasico.x50zo.model.listeners.GameEventAdapter;
import com.trifasico.x50zo.model.listeners.IGameEventListener;
import com.trifasico.x50zo.model.players.IPlayer;
import com.trifasico.x50zo.view.GameView;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * JavaFX controller orchestrating core loop events, state timing delays, and keyboard maps.
 *
 * <p>Bridges user interaction streams to underlying data models. Implements strict state control
 * parameters to block race-conditions on sequential execution steps during elimination windows.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.2
 */
public class GameController {

    @FXML private Label     turnPlayerLabel;
    @FXML private Label     tableSumLabel;
    @FXML private ImageView topCardView;
    @FXML private Label     statusLabel;
    @FXML private HBox      humanHandBox;
    @FXML private Label     humanNameLabel;
    @FXML private ImageView deckView;
    @FXML private Label     deckCountLabel;
    @FXML private HBox      topMachineArea;    // Handles Machine 1 and 2
    @FXML private VBox      rightMachineArea;  // Isolated container for Machine 3
    @FXML private Label     centerMessageLabel;

    private TurnManager turnManager;
    private GameView    view;
    private int         hoveredIndex = -1;
    private boolean     humanTurn    = false;
    private boolean     processingElimination = false;

    private static final int    MACHINE_COUNT = 3;
    private static final String HUMAN_NAME    = "Player 1";

    @FXML
    public void initialize() {
        this.view = new GameView(turnPlayerLabel, tableSumLabel, statusLabel, humanNameLabel, deckCountLabel,
                topCardView, deckView, humanHandBox, topMachineArea, rightMachineArea, centerMessageLabel);
        startNewGame();
    }

    @FXML
    public void onExitClicked() {
        ((Stage) turnPlayerLabel.getScene().getWindow()).close();
    }

    @FXML
    public void onHomeClicked() {
        // TODO: Main menu navigation route
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (!humanTurn) return;
        KeyCode code = event.getCode();

        if      (code == KeyCode.DIGIT1) applyKeyboardHover(0);
        else if (code == KeyCode.DIGIT2) applyKeyboardHover(1);
        else if (code == KeyCode.DIGIT3) applyKeyboardHover(2);
        else if (code == KeyCode.DIGIT4) applyKeyboardHover(3);
        else if (code == KeyCode.ENTER)  playHovered();
        else if (code == KeyCode.ESCAPE) clearHover();
    }

    private void startNewGame() {
        hoveredIndex = -1;
        humanTurn    = false;
        processingElimination = false;

        turnManager = new TurnManager(HUMAN_NAME, MACHINE_COUNT);
        turnManager.setEventListener(buildListener());

        try {
            turnManager.startGame();
        } catch (EmptyDeckException e) {
            view.setStatus("Deal failed: " + e.getMessage());
        }

        renderAll();

        Platform.runLater(() -> {
            turnPlayerLabel.getScene().setOnKeyPressed(this::onKeyPressed);
            turnPlayerLabel.getScene().getRoot().requestFocus();
        });
    }

    private void applyKeyboardHover(int index) {
        List<Card> hand = turnManager.getHuman().getHand();
        if (index >= hand.size()) return;
        setHovered(index);
    }

    private void playHovered() {
        if (hoveredIndex < 0) return;
        executePlay(hoveredIndex);
    }

    private void executePlay(int index) {
        if (!humanTurn) return;
        humanTurn = false;
        setUiEnabled(false);

        try {
            turnManager.humanPlayCard(index);
        } catch (InvalidPlayException e) {
            view.showCenterMessage("INVALID PLAY", "#FF4444", true);
            humanTurn = true;
            setUiEnabled(true);
        } catch (EmptyDeckException e) {
            view.setStatus("Deck error: " + e.getMessage());
        }

        hoveredIndex = -1;
    }

    private void launchMachineTurn() {
        setUiEnabled(false);
        new MachinePlayThread(turnManager, e ->
                Platform.runLater(() -> view.setStatus("Machine error: " + e.getMessage()))
        ).start();
    }

    private void setHovered(int index) {
        clearHoverVisuals();
        hoveredIndex = index;
        view.setHoverEffect(index, true);
    }

    private void clearHover() {
        clearHoverVisuals();
        hoveredIndex = -1;
    }

    private void clearHoverVisuals() {
        view.clearAllHoverEffects();
    }

    private void setUiEnabled(boolean enabled) {
        humanHandBox.setDisable(!enabled);
    }

    private List<IPlayer> getMachinePlayers() {
        return turnManager.getActivePlayers().stream()
                .filter(p -> !p.getName().equals(HUMAN_NAME))
                .toList();
    }

    private IGameEventListener buildListener() {
        return new GameEventAdapter() {

            @Override
            public void onTurnStarted(IPlayer player) {
                if (processingElimination) {
                    // Intercept and halt execution rhythm so elimination UI remains visible
                    processingElimination = false;
                    PauseTransition halt = new PauseTransition(Duration.seconds(2.0));
                    halt.setOnFinished(ev -> setupTurnState(player));
                    halt.play();
                } else {
                    setupTurnState(player);
                }
            }

            private void setupTurnState(IPlayer player) {
                boolean isHuman = player.getName().equals(HUMAN_NAME);
                view.setTurnStyleClass("turn-label-normal"); // Revert back to normal CSS classes
                view.updateTurn(isHuman ? "YOUR TURN" : player.getName().toUpperCase());
                view.setStatus("");
                clearHover();
                view.updateDeck(turnManager.remainingDeckCards());

                if (isHuman) {
                    if (!hasValidPlays()) {
                        view.showCenterMessage("YOU LOSE", "#FF4444", false);
                        setUiEnabled(false);

                        PauseTransition pause = new PauseTransition(Duration.seconds(2));
                        pause.setOnFinished(ev -> turnManager.eliminateCurrentPlayer());
                        pause.play();
                    } else {
                        humanTurn = true;
                        setUiEnabled(true);
                        renderHumanHand();
                    }
                } else {
                    humanTurn = false;
                    launchMachineTurn();
                }
            }

            @Override
            public void onCardPlayed(IPlayer player, Card card, int newSum) {
                view.animateCardPlay(card);
                view.updateSum(newSum);
                renderHumanHand();
                view.renderMachineHands(getMachinePlayers());
                view.updateDeck(turnManager.remainingDeckCards());
            }

            @Override
            public void onPlayerEliminated(IPlayer eliminated) {
                processingElimination = true;
                view.updateTurn(eliminated.getName().toUpperCase() + " ELIMINATED!");
                view.setTurnStyleClass("turn-label-eliminated"); // Trigger custom layout look via CSS
                view.renderMachineHands(getMachinePlayers());    // Remove their cards instantly
            }

            @Override
            public void onGameOver(IPlayer winner) {
                humanTurn = false;
                setUiEnabled(false);

                if (winner != null) {
                    if (winner.getName().equals(HUMAN_NAME)) {
                        view.showCenterMessage("YOU WIN", "#00FFAA", false);
                    } else {
                        view.showCenterMessage(winner.getName().toUpperCase() + " WON", "#FFD700", false);
                    }
                } else {
                    view.showCenterMessage("DRAW", "#FFFFFF", false);
                }

                PauseTransition pause = new PauseTransition(Duration.seconds(3.5));
                pause.setOnFinished(e -> ((Stage) turnPlayerLabel.getScene().getWindow()).close());
                pause.play();
            }

            @Override
            public void onDeckReshuffled() {
                view.setStatus("Deck reshuffled.");
                view.updateDeck(turnManager.remainingDeckCards());
            }
        };
    }

    private void renderAll() {
        renderHumanHand();
        view.renderMachineHands(getMachinePlayers());
        view.setTopCard(turnManager.getTopCard());
        view.updateDeck(turnManager.remainingDeckCards());
        view.setDeckImage();
        view.updateSum(turnManager.getTableSum());
        humanNameLabel.setText(HUMAN_NAME);
    }

    private void renderHumanHand() {
        view.renderHumanHand(turnManager.getHuman().getHand(),
                index -> new CardHoverHandler(index, true, this),
                index -> new CardHoverHandler(index, false, this),
                index -> new CardHoverHandler(index, false, this, true)
        );
    }

    private boolean hasValidPlays() {
        int currentSum = turnManager.getTableSum();
        for (Card card : turnManager.getHuman().getHand()) {
            if (currentSum + card.getValue(currentSum) <= 50) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================

    /**
     * Inner event-handler tracking human interaction mechanics across individual rendering slots.
     */
    static class CardHoverHandler implements javafx.event.EventHandler<MouseEvent> {

        private final int            index;
        private final boolean        entering;
        private final boolean        clicking;
        private final GameController controller;

        CardHoverHandler(int index, boolean entering, GameController controller) {
            this(index, entering, controller, false);
        }

        CardHoverHandler(int index, boolean entering, GameController controller, boolean clicking) {
            this.index      = index;
            this.entering   = entering;
            this.clicking   = clicking;
            this.controller = controller;
        }

        @Override
        public void handle(MouseEvent event) {
            if (!controller.humanTurn) return;

            if (clicking) {
                controller.executePlay(index);
            } else if (entering) {
                controller.setHovered(index);
            } else {
                controller.clearHoverVisuals();
                controller.hoveredIndex = -1;
            }
        }
    }
}