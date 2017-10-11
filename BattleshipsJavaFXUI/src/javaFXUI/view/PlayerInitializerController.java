package javaFXUI.view;

import javaFXUI.Constants;
import javaFXUI.JavaFXManager;
import javaFXUI.model.PlayerAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

public class PlayerInitializerController {
    @FXML
    private ImageView imageViewProfilePic;
    @FXML
    private Label labelPlayerNumber;
    @FXML
    private TextField textFieldPlayerName;
    @FXML
    private Button buttonOK;

    private JavaFXManager javaFXManager;
    private PlayerAdapter player;
    private Image playerImage;

    @FXML
    void buttonOK_Clicked(ActionEvent event) {
        updatePlayerInfo();
        javaFXManager.playerWindowOkClicked();
    }

    public void updatePlayerInfo() {
        player.setName(textFieldPlayerName.getText());
        player.setPlayerImage(imageViewProfilePic.getImage());
    }

    public void setOwnerWindow(Window ownerWindow, JavaFXManager javaFXManager) {
        Window ownerWindow1 = ownerWindow;
        this.javaFXManager = javaFXManager;
    }

    public void setPlayer(PlayerAdapter player, int playerNumber) {
        this.player = player;
        labelPlayerNumber.setText("Player " + playerNumber);
        textFieldPlayerName.setText(player.getName());
        if (playerNumber == 1) {
            imageViewProfilePic.setImage(new Image(Constants.PLAYER1_IMAGE_URL));
        } else {
            imageViewProfilePic.setImage(new Image(Constants.PLAYER2_IMAGE_URL));
        }
    }
}
