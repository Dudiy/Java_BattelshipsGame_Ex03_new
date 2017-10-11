package javaFXUI.model;

import javafx.concurrent.Task;

public class StartGameTask extends Task<Boolean> {

    @Override
    protected Boolean call() throws Exception {
        for (int i = 0; i < 100; i++) {
            updateProgress(i, 100);
            Thread.sleep(25);
        }
        return true;
    }

    public void resetProgress(){
        updateProgress(0,100);
    }
}
