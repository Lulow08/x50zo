package com.trifasico.x50zo.view;

import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.players.IPlayer;
import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Handles all UI rendering, layout coordination, and visual animations for the game screen.
 *
 * <p>Following the MVC paradigm, this class treats all nodes as passive visual containers.
 * It coordinates the architectural separation of opposing player hands across the screen walls
 * and encapsulates all JavaFX transition layers.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.2
 */
public class GameView {

    private final Label turnPlayerLabel, tableSumLabel, statusLabel, humanNameLabel, deckCountLabel, centerMessageLabel;
    private final ImageView topCardView, deckView;
    private final HBox humanHandBox, topMachineArea;
    private final VBox rightMachineArea;

    private static final String CARD_SPR = "/sprites/%s_%s.png";
    private static final String BACK_SPR = "/sprites/back.png";
    private static final double HOVER_Y   = -18.0;
    private static final int    ANIM_MS   = 120;

    private final List<StackPane> cardSlots = new ArrayList<>();

    public GameView(Label turnPlayer, Label tableSum, Label status, Label humanName, Label deckCount,
                    ImageView topCard, ImageView deck, HBox humanHand, HBox topMachineArea,
                    VBox rightMachineArea, Label centerMsg) {
        this.turnPlayerLabel = turnPlayer;
        this.tableSumLabel = tableSum;
        this.statusLabel = status;
        this.humanNameLabel = humanName;
        this.deckCountLabel = deckCount;
        this.topCardView = topCard;
        this.deckView = deck;
        this.humanHandBox = humanHand;
        this.topMachineArea = topMachineArea;
        this.rightMachineArea = rightMachineArea;
        this.centerMessageLabel = centerMsg;
    }

    public void updateTurn(String playerName) {
        turnPlayerLabel.setText(playerName);
    }

    /**
     * Updates the CSS style classes of the turn label to allow for distinct
     * semantic rendering (e.g., normal turn vs player elimination alerts).
     *
     * @param styleClass the target CSS class defined in your stylesheet
     */
    public void setTurnStyleClass(String styleClass) {
        turnPlayerLabel.getStyleClass().clear();
        turnPlayerLabel.getStyleClass().add(styleClass);
    }

    public void updateSum(int sum) {
        tableSumLabel.setText(String.valueOf(sum));
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    public void updateDeck(int remaining) {
        deckCountLabel.setText(remaining + "/52");
    }

    public void setTopCard(Card card) {
        if (card != null) topCardView.setImage(loadCardImage(card));
    }

    public void setDeckImage() {
        if (deckView.getImage() == null) {
            deckView.setImage(loadImage(BACK_SPR));
        }
    }

    public void renderHumanHand(List<Card> hand,
                                Function<Integer, EventHandler<MouseEvent>> enterFactory,
                                Function<Integer, EventHandler<MouseEvent>> exitFactory,
                                Function<Integer, EventHandler<MouseEvent>> clickFactory) {
        humanHandBox.getChildren().clear();
        cardSlots.clear();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            ImageView cardSprite = new ImageView(loadCardImage(card));
            cardSprite.setFitWidth(90);
            cardSprite.setFitHeight(130);
            cardSprite.setPreserveRatio(true);

            StackPane slot = new StackPane(cardSprite);
            slot.getStyleClass().add("card-slot");

            slot.addEventHandler(MouseEvent.MOUSE_ENTERED, enterFactory.apply(i));
            slot.addEventHandler(MouseEvent.MOUSE_EXITED, exitFactory.apply(i));
            slot.addEventHandler(MouseEvent.MOUSE_CLICKED, clickFactory.apply(i));

            cardSlots.add(slot);
            humanHandBox.getChildren().add(slot);
        }
    }

    /**
     * Distributes machine player interfaces across layout anchors depending on active count.
     * Machines 1 and 2 are rendered side by side within the top boundary box. Machine 3 is isolated
     * and rotated counter-clockwise inside the right-hand boundary container facing inward.
     */
    public void renderMachineHands(List<IPlayer> machines) {
        topMachineArea.getChildren().clear();
        rightMachineArea.getChildren().clear();

        int totalMachines = machines.size();

        for (int i = 0; i < totalMachines; i++) {
            IPlayer player = machines.get(i);

            Label nameLabel = new Label(player.getName().toUpperCase());
            nameLabel.getStyleClass().add("machine-name");

            HBox handBox = new HBox(4);
            handBox.setAlignment(Pos.CENTER);

            for (int h = 0; h < player.handSize(); h++) {
                ImageView iv = new ImageView(loadImage(BACK_SPR));
                iv.setFitWidth(38);
                iv.setFitHeight(55);
                iv.setPreserveRatio(true);
                handBox.getChildren().add(iv);
            }

            VBox machineContainer = new VBox(4);
            machineContainer.getStyleClass().add("machine-box");
            machineContainer.setAlignment(Pos.CENTER);
            machineContainer.getChildren().addAll(nameLabel, handBox);

            // Isolate the 3rd machine to the right side if 3 players are active
            if (totalMachines == 3 && i == 2) {
                // Rotates 90 degrees counter-clockwise so text and cards point inward
                machineContainer.setRotate(-90);
                rightMachineArea.getChildren().add(machineContainer);
            } else {
                topMachineArea.getChildren().add(machineContainer);
            }
        }
    }

    public void animateCardPlay(Card card) {
        topCardView.setTranslateY(60);
        topCardView.setOpacity(0);
        topCardView.setImage(loadCardImage(card));

        TranslateTransition tt = new TranslateTransition(Duration.millis(ANIM_MS), topCardView);
        tt.setFromY(60); tt.setToY(0); tt.play();

        FadeTransition ft = new FadeTransition(Duration.millis(ANIM_MS), topCardView);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    public void showCenterMessage(String msg, String colorHex, boolean autoHide) {
        centerMessageLabel.setText(msg);
        centerMessageLabel.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0.5, 0, 0);",
                colorHex
        ));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), centerMessageLabel);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        if (autoHide) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), centerMessageLabel);
            fadeOut.setFromValue(1); fadeOut.setToValue(0); fadeOut.setDelay(Duration.seconds(2));
            new SequentialTransition(fadeIn, fadeOut).play();
        } else {
            fadeIn.play();
        }
    }

    public void setHoverEffect(int index, boolean hover) {
        if (index >= 0 && index < cardSlots.size()) {
            cardSlots.get(index).setTranslateY(hover ? HOVER_Y : 0);
        }
    }

    public void clearAllHoverEffects() {
        for (StackPane slot : cardSlots) {
            slot.setTranslateY(0);
        }
    }

    private Image loadCardImage(Card card) {
        return loadImage(String.format(CARD_SPR, card.suit(), card.rank().name().toLowerCase()));
    }

    private Image loadImage(String path) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(BACK_SPR)));
        }
    }
}