package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.TurnManager;
import com.trifasico.x50zo.model.listeners.GameEventAdapter;
import com.trifasico.x50zo.model.listeners.IGameEventListener;
import com.trifasico.x50zo.model.players.IPlayer;
import com.trifasico.x50zo.view.GameView;
import com.trifasico.x50zo.view.SceneManager;

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
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/**
 * Controller class orchestrating core game loop events, state timing, and keyboard interactions.
 * Bridges user interaction streams to underlying data models.
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
    @FXML private HBox      topMachineArea;
    @FXML private VBox      rightMachineArea;
    @FXML private Label     centerMessageLabel;

    private TurnManager turnManager;
    private GameView    view;
    private int         hoveredIndex = -1;
    private boolean     humanTurn    = false;
    private boolean     processingElimination = false;
    private int         machineCount = 1;

    private static final String HUMAN_NAME = "Player 1";

    /**
     * Initializes the game controller and instantiates the view wrapper.
     */
    @FXML
    public void initialize() {
        this.view = new GameView(turnPlayerLabel, tableSumLabel, statusLabel, humanNameLabel, deckCountLabel,
                topCardView, deckView, humanHandBox, topMachineArea, rightMachineArea, centerMessageLabel);
    }

    /**
     * Sets the number of machine opponents for the upcoming game instance.
     *
     * @param bots The amount of machine players to be generated.
     */
    public void setBotCount(int bots) {
        this.machineCount = bots;
    }

    /**
     * Terminates the application gracefully.
     */
    @FXML
    public void onExitClicked() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Redirects the user back to the main menu view.
     */
    @FXML
    public void onHomeClicked() {
        try {
            SceneManager.getInstance().switchScene("menu-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Captures and processes keyboard events to allow non-mouse gameplay during a human turn.
     *
     * @param event The keyboard event detected.
     */
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

    /**
     * Initializes the underlying logic models and starts a new instance of the game loop.
     */
    public void startNewGame() {
        hoveredIndex = -1;
        humanTurn    = false;
        processingElimination = false;

        turnManager = new TurnManager(HUMAN_NAME, machineCount);
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

    /**
     * Simulates mouse hovering through keyboard input.
     *
     * @param index The index of the card to target.
     */
    private void applyKeyboardHover(int index) {
        List<Card> hand = turnManager.getHuman().getHand();
        if (index >= hand.size()) return;
        setHovered(index);
    }

    /**
     * Plays the currently hovered card based on the active index.
     */
    private void playHovered() {
        if (hoveredIndex < 0) return;
        executePlay(hoveredIndex);
    }

    /**
     * Executes the logic to process a human turn given a specific card index.
     *
     * @param index The index of the card to be played from the hand.
     */
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

    /**
     * Initiates a background thread to process a machine opponent's move.
     */
    private void launchMachineTurn() {
        setUiEnabled(false);
        new MachinePlayThread(turnManager, e ->
                Platform.runLater(() -> view.setStatus("Machine error: " + e.getMessage()))
        ).start();
    }

    /**
     * Sets the active hovered index and commands the view to display the appropriate effect.
     *
     * @param index The index of the targeted card.
     */
    private void setHovered(int index) {
        clearHoverVisuals();
        hoveredIndex = index;
        view.setHoverEffect(index, true);
    }

    /**
     * Clears local hover tracking integers and removes hover effects from the view.
     */
    private void clearHover() {
        clearHoverVisuals();
        hoveredIndex = -1;
    }

    /**
     * Triggers the view to clear any current hovering style modifications.
     */
    private void clearHoverVisuals() {
        view.clearAllHoverEffects();
    }

    /**
     * Enables or disables interaction with the human hand box layout.
     *
     * @param enabled The boolean value dictating interactability.
     */
    private void setUiEnabled(boolean enabled) {
        humanHandBox.setDisable(!enabled);
    }

    /**
     * Retrieves the current list of active machine opponents.
     *
     * @return A list containing active {@link IPlayer} instances excluding the human.
     */
    private List<IPlayer> getMachinePlayers() {
        return turnManager.getActivePlayers().stream()
                .filter(p -> !p.getName().equals(HUMAN_NAME))
                .toList();
    }

    /**
     * Constructs and binds the adapter required to listen to logic model events.
     *
     * @return An implementation of {@link IGameEventListener}.
     */
    private IGameEventListener buildListener() {
        return new GameEventAdapter() {

            @Override
            public void onTurnStarted(IPlayer player) {
                if (processingElimination) {
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
                view.setTurnStyleClass("turn-label-normal");
                view.updateTurn(isHuman ? "YOUR TURN" : player.getName().toUpperCase());
                view.setStatus("");
                clearHover();
                view.updateDeck(turnManager.remainingDeckCards());

                if (isHuman) {
                    if (!hasValidPlays()) {
                        view.showCenterMessage("YOU LOSE", "#FF4444", false);
                        setUiEnabled(false);

                        PauseTransition pause = new PauseTransition(Duration.seconds(3.0));
                        pause.setOnFinished(ev -> {
                            try {
                                SceneManager.getInstance().switchScene("menu-view.fxml");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
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
                view.setTurnStyleClass("turn-label-eliminated");
                view.renderMachineHands(getMachinePlayers());
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
                pause.setOnFinished(e -> {
                    try {
                        SceneManager.getInstance().switchScene("menu-view.fxml");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                pause.play();
            }

            @Override
            public void onDeckReshuffled() {
                view.setStatus("Deck reshuffled.");
                view.updateDeck(turnManager.remainingDeckCards());
            }
        };
    }

    /**
     * Executes the rendering process for all major components inside the view class.
     */
    private void renderAll() {
        renderHumanHand();
        view.renderMachineHands(getMachinePlayers());
        view.setTopCard(turnManager.getTopCard());
        view.updateDeck(turnManager.remainingDeckCards());
        view.setDeckImage();
        view.updateSum(turnManager.getTableSum());
        humanNameLabel.setText(HUMAN_NAME);
    }

    /**
     * Re-draws the human player hand and resets event handlers for visual slots.
     */
    private void renderHumanHand() {
        view.renderHumanHand(turnManager.getHuman().getHand(),
                index -> new CardHoverHandler(index, true, this),
                index -> new CardHoverHandler(index, false, this),
                index -> new CardHoverHandler(index, false, this, true)
        );
    }

    /**
     * Checks if the human player currently has any valid play left based on the table sum.
     *
     * @return True if a valid play exists, otherwise false.
     */
    private boolean hasValidPlays() {
        int currentSum = turnManager.getTableSum();
        for (Card card : turnManager.getHuman().getHand()) {
            if (currentSum + card.getValue(currentSum) <= 50) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inner utility class serving as a mouse event handler for individual card slots.
     */
    static class CardHoverHandler implements javafx.event.EventHandler<MouseEvent> {

        private final int            index;
        private final boolean        entering;
        private final boolean        clicking;
        private final GameController controller;

        /**
         * Constructs a new handler dedicated to hover detection.
         *
         * @param index The zero-based index of the card slot in the hand.
         * @param entering Whether this handles a mouse enter or mouse exit phase.
         * @param controller The active GameController instance orchestrating logic.
         */
        CardHoverHandler(int index, boolean entering, GameController controller) {
            this(index, entering, controller, false);
        }

        /**
         * Constructs a new handler supporting mouse clicks alongside hovering.
         *
         * @param index The zero-based index of the card slot in the hand.
         * @param entering Whether this handles a mouse enter or mouse exit phase.
         * @param controller The active GameController instance orchestrating logic.
         * @param clicking Whether this execution involves a primary click.
         */
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