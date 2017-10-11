package gameLogic.game;

import java.io.Serializable;

public enum eGameState implements Serializable {
    INVALID("game not loaded from xml file"),
    LOADED("game successfully loaded from xml file"),
    INITIALIZED("game loaded and initialized"),
    STARTED("game Started"),
    PLAYER_WON("A player has won"),
    PLAYER_QUIT("A player has chosen to quit");

    private String name;

    eGameState(String name) {
        this.name = name;
    }

    public boolean gameHasStarted() {
        return this != INVALID &&
                this != LOADED &&
                this != INITIALIZED;
    }

    public boolean isGameEnded() {
        return this == PLAYER_WON || this == PLAYER_QUIT;
    }

    @Override
    public String toString() {
        return name;
    }
}
