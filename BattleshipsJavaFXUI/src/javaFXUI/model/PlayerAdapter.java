package javaFXUI.model;

import gameLogic.users.Player;
import javafx.scene.image.Image;

public class PlayerAdapter extends Player {
    Image playerImage;

    public PlayerAdapter(String playerID, String playerName) {
        super(playerID, playerName);
    }

    public PlayerAdapter(String playerID, String playerName, Image playerImage) {
        super(playerID, playerName);
        this.playerImage = playerImage;
    }

    public Image getPlayerImage() {
        return playerImage;
    }

    public void setPlayerImage(Image playerImage) {
        this.playerImage = playerImage;
    }
}
