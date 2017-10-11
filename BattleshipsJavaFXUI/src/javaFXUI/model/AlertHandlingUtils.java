package javaFXUI.model;

import javafx.scene.control.Alert;

public class AlertHandlingUtils {
    public static void showErrorMessage(Exception exception, String headerText) {
        String contentText = "Error Message: \n" + exception.getMessage();
        showErrorMessage(exception, headerText, contentText);
    }

    public static void showErrorMessage(Exception exception, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Huston...We have a problem!");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
