package javaFXUI.view;

import gameLogic.users.Player;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameEndedLayoutController {

    @FXML
    private Label labelWinnerName;
    @FXML
    private Label labelPlayer1Name;
    @FXML
    private Label labelPlayer1Score;
    @FXML
    private Label labelPlayer2Name;
    @FXML
    private Label labelPlayer2Score;

    private SimpleStringProperty winnerName = new SimpleStringProperty();
    private SimpleStringProperty player1Name = new SimpleStringProperty();
    private SimpleStringProperty player2Name = new SimpleStringProperty();
    private SimpleIntegerProperty player1Score = new SimpleIntegerProperty();
    private SimpleIntegerProperty player2Score = new SimpleIntegerProperty();

    @FXML
    private void initialize(){
        labelWinnerName.textProperty().bind(winnerName);
        labelPlayer1Name.textProperty().bind(player1Name);
        labelPlayer1Score.textProperty().bind(player1Score.asString());
        labelPlayer2Name.textProperty().bind(player2Name);
        labelPlayer2Score.textProperty().bind(player2Score.asString());
    }

    public void setPlayers(Player[] players) {
        player1Name.setValue(players[0].getName());
        player1Score.setValue(players[0].getScore());
        player2Name.setValue(players[1].getName());
        player2Score.setValue(players[1].getScore());
    }

    public void setWinnerName(String winnerName) {
        this.winnerName.set(winnerName);
    }
}
