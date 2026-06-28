package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.TurnManager;
import com.trifasico.x50zo.model.listeners.GameEventAdapter;
import com.trifasico.x50zo.model.listeners.IGameEventListener;
import com.trifasico.x50zo.model.players.IPlayer;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JavaFX controller for the main game screen ({@code game-view.fxml}).
 *
 * <p>Acts as the boundary between the FXML view and the model layer
 * ({@link TurnManager}). No game logic lives here — the controller only
 * translates UI events into model calls and model callbacks into UI updates.</p>
 *
 * <h2>Interaction model</h2>
 * <ul>
 *   <li><strong>Mouse hover</strong> — card rises (CSS {@code translateY}).</li>
 *   <li><strong>Mouse click</strong> — immediately plays the hovered card.</li>
 *   <li><strong>Number keys 1–4</strong> — hover the matching card without
 *       playing; mouse re-hover always overrides keyboard hover.</li>
 *   <li><strong>Enter</strong> — plays the currently hovered card (if any).</li>
 *   <li><strong>Escape</strong> — clears keyboard hover.</li>
 * </ul>
 *
 * <h2>Inner class</h2>
 * <p>{@link CardHoverHandler} is a named static inner class that encapsulates
 * all mouse events for a single card slot, fulfilling the rubric requirement
 * for an inner class while keeping event logic co-located with its owner.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see TurnManager
 * @see MachinePlayThread
 */
public class GameController {

    private static final String CARD_SPR = "/sprites/%s_%s.png";
    private static final String BACK_SPR = "/sprites/back.png";
    private static final double HOVER_Y   = -18.0;
    private static final int    ANIM_MS   = 120;

    @FXML private Label     turnPlayerLabel;
    @FXML private Label     tableSumLabel;
    @FXML private ImageView topCardView;
    @FXML private Label     statusLabel;
    @FXML private HBox      humanHandBox;
    @FXML private Label     humanNameLabel;
    @FXML private ImageView deckView;
    @FXML private Label     deckCountLabel;
    @FXML private HBox      machineArea;

    private TurnManager        turnManager;
    private final List<StackPane> cardSlots = new ArrayList<>();
    private int hoveredIndex = -1;
    private boolean humanTurn = false;

    private static final int    MACHINE_COUNT = 1;
    private static final String HUMAN_NAME    = "Player 1";

    @FXML
    public void initialize() {
        startNewGame();
    }

    @FXML
    public void onExitClicked() {
        Stage stage = (Stage) turnPlayerLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onHomeClicked() {
        // TODO: navigate to menu-view when implemented
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

        turnManager = new TurnManager(HUMAN_NAME, MACHINE_COUNT);
        turnManager.setEventListener(buildListener());

        try {
            turnManager.startGame();
        } catch (EmptyDeckException e) {
            showStatus("Deal failed: " + e.getMessage());
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
        System.out.println("executePlay called, humanTurn=" + humanTurn + ", index=" + index);
        if (!humanTurn) return;
        humanTurn = false;
        setUiEnabled(false);

        try {
            turnManager.humanPlayCard(index);
        } catch (InvalidPlayException e) {
            showStatus("That card would exceed 50 — pick another.");
            humanTurn = true;
            setUiEnabled(true);
        } catch (EmptyDeckException e) {
            showStatus("Deck error: " + e.getMessage());
        }

        hoveredIndex = -1;
    }

    private void launchMachineTurn() {
        setUiEnabled(false);
        MachinePlayThread thread = new MachinePlayThread(turnManager, e ->
                Platform.runLater(() -> showStatus("Machine error: " + e.getMessage()))
        );
        thread.start();
    }

    private void setHovered(int index) {
        clearHoverVisuals();
        hoveredIndex = index;
        if (index >= 0 && index < cardSlots.size()) {
            cardSlots.get(index).setTranslateY(HOVER_Y);
        }
    }

    private void clearHover() {
        clearHoverVisuals();
        hoveredIndex = -1;
    }

    private void clearHoverVisuals() {
        for (StackPane slot : cardSlots) {
            slot.setTranslateY(0);
        }
    }

    private void setUiEnabled(boolean enabled) {
        humanHandBox.setDisable(!enabled);
    }

    private IGameEventListener buildListener() {
        return new GameEventAdapter() {

            @Override
            public void onTurnStarted(IPlayer player) {
                boolean isHuman = player.getName().equals(HUMAN_NAME);
                System.out.println("onTurnStarted: " + player.getName() + ", isHuman=" + isHuman);
                turnPlayerLabel.setText(isHuman ? "YOU" : player.getName());
                showStatus("");
                clearHover();
                renderDeck();

                if (isHuman) {
                    humanTurn = true;
                    setUiEnabled(true);
                    renderHumanHand();
                } else {
                    humanTurn = false;
                    launchMachineTurn();
                }
            }

            @Override
            public void onCardPlayed(IPlayer player, Card card, int newSum) {
                animateCardPlay(card);
                tableSumLabel.setText(String.valueOf(newSum));
                renderHumanHand();
                renderMachineHands();
                renderDeck();
            }

            @Override
            public void onPlayerEliminated(IPlayer eliminated) {
                showStatus(eliminated.getName() + " eliminated!");
                renderMachineHands();
            }

            @Override
            public void onGameOver(IPlayer winner) {
                humanTurn = false;
                setUiEnabled(false);
                String msg = (winner != null)
                        ? (winner.getName().equals(HUMAN_NAME) ? "You win!" : winner.getName() + " wins!")
                        : "No survivors.";
                showStatus(msg);

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> {
                    Stage stage = (Stage) turnPlayerLabel.getScene().getWindow();
                    stage.close();
                });
                pause.play();
            }

            @Override
            public void onDeckReshuffled() {
                showStatus("Deck reshuffled.");
                renderDeck();
            }
        };
    }

    private void animateCardPlay(Card card) {
        topCardView.setTranslateY(60);
        topCardView.setOpacity(0);
        topCardView.setImage(loadCardImage(card));

        TranslateTransition tt = new TranslateTransition(Duration.millis(ANIM_MS), topCardView);
        tt.setFromY(60);
        tt.setToY(0);
        tt.play();

        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(ANIM_MS), topCardView);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void renderAll() {
        renderHumanHand();
        renderMachineHands();
        renderTopCard();
        renderDeck();
        tableSumLabel.setText(String.valueOf(turnManager.getTableSum()));
        humanNameLabel.setText(HUMAN_NAME);
    }

    private void renderHumanHand() {
        humanHandBox.getChildren().clear();
        cardSlots.clear();

        List<Card> hand = turnManager.getHuman().getHand();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            ImageView cardSprite = new ImageView(loadCardImage(card));
            cardSprite.setFitWidth(90);
            cardSprite.setFitHeight(130);
            cardSprite.setPreserveRatio(true);

            StackPane slot = new StackPane(cardSprite);
            slot.getStyleClass().add("card-slot");

            slot.addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new CardHoverHandler(i, true,  this));
            slot.addEventHandler(MouseEvent.MOUSE_EXITED,
                    new CardHoverHandler(i, false, this));
            slot.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    new CardHoverHandler(i, false, this, true));

            cardSlots.add(slot);
            humanHandBox.getChildren().add(slot);
        }
    }

    private void renderMachineHands() {
        machineArea.getChildren().clear();

        List<IPlayer> machines = turnManager.getActivePlayers().stream()
                .filter(p -> !p.getName().equals(HUMAN_NAME))
                .toList();

        int count = machines.size();

        for (int m = 0; m < count; m++) {
            IPlayer player = machines.get(m);

            Label nameLabel = new Label(player.getName().toUpperCase());
            nameLabel.getStyleClass().add("machine-name");

            HBox handBox = new HBox(4);
            handBox.setAlignment(javafx.geometry.Pos.CENTER);

            for (int i = 0; i < player.handSize(); i++) {
                ImageView iv = new ImageView(loadImage(BACK_SPR));
                iv.setFitWidth(38);
                iv.setFitHeight(55);
                iv.setPreserveRatio(true);
                handBox.getChildren().add(iv);
            }

            VBox machineBox = new VBox(4);
            machineBox.getStyleClass().add("machine-box");
            machineBox.setAlignment(javafx.geometry.Pos.CENTER);

            if (count == 3 && m == 2) {
                machineBox.setRotate(90);
            }

            machineBox.getChildren().addAll(nameLabel, handBox);
            machineArea.getChildren().add(machineBox);
        }
    }

    private void renderTopCard() {
        Card top = turnManager.getTopCard();
        if (top != null) topCardView.setImage(loadCardImage(top));
    }

    private void renderDeck() {
        deckCountLabel.setText(turnManager.remainingDeckCards() + "/52");
        if (deckView.getImage() == null) {
            deckView.setImage(loadImage(BACK_SPR));
        }
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
    }

    private Image loadCardImage(Card card) {
        String path = String.format(CARD_SPR,
                card.suit(),
                card.rank().name().toLowerCase());
        return loadImage(path);
    }

    private Image loadImage(String path) {
        try {
            return new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream(path)));
        } catch (NullPointerException e) {
            return new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream(BACK_SPR)));
        }
    }

    // =========================================================================

    /**
     * Inner class that handles all mouse interaction events for a single card
     * slot in the human player's hand.
     *
     * <p>A named static inner class is used instead of lambdas so that each
     * of the three distinct event types (enter, exit, click) shares the same
     * handler structure and remains readable without repetition. This also
     * fulfills the rubric requirement for an inner class.</p>
     *
     * <p>Mouse interaction rules:</p>
     * <ul>
     *   <li>Enter → hover the card (raise it); overrides keyboard hover.</li>
     *   <li>Exit  → un-hover this card only; keyboard hover is not restored.</li>
     *   <li>Click → play the card immediately.</li>
     * </ul>
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

        /**
         * Handles MOUSE_ENTERED, MOUSE_EXITED, and MOUSE_CLICKED on a card slot.
         *
         * @param event the mouse event from JavaFX
         */
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