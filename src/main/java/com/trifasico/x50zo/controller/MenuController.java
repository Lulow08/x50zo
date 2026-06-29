package com.trifasico.x50zo.controller;

import com.trifasico.x50zo.view.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller class for the main menu view.
 * Handles bot selection logic and navigation to the game scene.
 */
public class MenuController {

    @FXML
    private ImageView botCountCardView;

    @FXML
    private Button leftArrowBtn;

    @FXML
    private Button rightArrowBtn;

    private int numberOfBots = 1;

    private final String[] botCards = {
            "spades_ace",
            "spades_two",
            "spades_three"
    };

    /**
     * Initializes the controller class. Automatically invoked after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        updateCardAndButtons();
    }

    /**
     * Decreases the selected number of bots if greater than the minimum limit.
     */
    @FXML
    private void onDecreaseBots() {
        if (numberOfBots > 1) {
            numberOfBots--;
            updateCardAndButtons();
        }
    }

    /**
     * Increases the selected number of bots if less than the maximum limit.
     */
    @FXML
    private void onIncreaseBots() {
        if (numberOfBots < 3) {
            numberOfBots++;
            updateCardAndButtons();
        }
    }

    /**
     * Updates the user interface to reflect the currently selected number of bots.
     */
    private void updateCardAndButtons() {
        String imagePath = "/sprites/" + botCards[numberOfBots - 1] + ".png";
        try {
            Image cardImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            botCountCardView.setImage(cardImage);
        } catch (NullPointerException e) {
            System.err.println("Image not found: " + imagePath);
        }

        leftArrowBtn.setDisable(numberOfBots == 1);
        rightArrowBtn.setDisable(numberOfBots == 3);
    }

    /**
     * Handles the play action, loading the game scene and injecting the bot configuration.
     *
     * @param event The triggered ActionEvent.
     */
    @FXML
    private void onPlayClicked(ActionEvent event) {
        try {
            GameController gameController = SceneManager.getInstance().switchScene("game-view.fxml");
            gameController.setBotCount(numberOfBots);
            gameController.startNewGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates the application gracefully.
     */
    @FXML
    private void onExitClicked() {
        Platform.exit();
        System.exit(0);
    }
}