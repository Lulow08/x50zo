package com.trifasico.x50zo.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Singleton that owns the primary {@link Stage} and handles scene transitions.
 *
 * <p>All FXML files are expected under {@code /fxml/} on the classpath.
 * The stylesheet at {@code /css/styles.css} and the custom font under
 * {@code /fonts/} are loaded once and applied to every scene.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
public final class SceneManager {

    private static SceneManager instance;
    private Stage mainStage;
    private SceneManager() {}

    /**
     * Returns the singleton instance, creating it on first call.
     *
     * @return the {@code SceneManager} instance
     */
    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    /**
     * Sets the primary stage. Must be called once in {@code AppInitializer}
     * before any scene switch.
     *
     * @param stage the JavaFX primary stage
     */
    public void setMainStage(Stage stage) { this.mainStage = stage; }

    /**
     * Loads the custom font from the classpath so JavaFX CSS can reference it
     * by family name. Safe to call multiple times — subsequent calls are no-ops
     * because JavaFX caches loaded fonts.
     */
    public void loadFonts() {
        Font.loadFont(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/fonts/Lemon-Days.otf"),
                        "Font not found: " + "/fonts/Lemon-Days.otf"
                ), 14
        );

    }

    /**
     * Loads the given FXML file, wraps it in a {@link Scene} with the global
     * stylesheet applied, and sets it on the primary stage.
     *
     * @param <SceneController> the type of the FXML controller
     * @param fxmlFile filename only (e.g. {@code "game-view.fxml"})
     * @return the controller instance created by the {@link FXMLLoader}
     * @throws IOException if the FXML file cannot be found or parsed
     */
    public <SceneController> SceneController switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );

        mainStage.setScene(scene);
        mainStage.show();

        root.requestFocus();

        return loader.getController();
    }
}