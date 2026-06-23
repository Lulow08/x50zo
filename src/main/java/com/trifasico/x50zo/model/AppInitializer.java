package com.trifasico.x50zo.model;

import com.trifasico.x50zo.view.SceneManager;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AppInitializer {

    private static final String APP_TITLE    = "50zo";
    //private static final String FAVICON_PATH = "/icons/favicon.png";

    private static final String ENTRY_SCENE  = "game-view.fxml";
    public void start(Stage stage) throws IOException {
        /*Image favicon = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(FAVICON_PATH)
        ));*/

        stage.setTitle(APP_TITLE);
        stage.setResizable(false);
        //stage.getIcons().add(favicon);

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setMainStage(stage);
        sceneManager.loadFonts();
        sceneManager.switchScene(ENTRY_SCENE);
    }
}