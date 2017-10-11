package javaFXUI.view;

import javaFXUI.JavaFXManager;
import javaFXUI.model.StartGameTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class WelcomeSceneController {
    @FXML
    private ProgressBar taskProgressBar;

    private StartGameTask startGameTask = new StartGameTask();
    private JavaFXManager javaFXManager;
    private final BooleanProperty gameTaskFinished = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        taskProgressBar.progressProperty().bind(startGameTask.progressProperty());
    }

    public void setJavaFXManager(JavaFXManager javaFXManager) {
        this.javaFXManager = javaFXManager;
    }

    public void onStartGameSelected(){

        Thread taskThread = new Thread(() -> {
            startGameTask.run();
            progressBarFinished();
        });
        taskThread.start();
    }

    private void progressBarFinished(){
        gameTaskFinished.setValue(true);
        startGameTask.resetProgress();

    }

    public BooleanProperty gameTaskFinishedProperty() {
        return gameTaskFinished;
    }
}
