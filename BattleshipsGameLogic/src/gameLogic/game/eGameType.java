package gameLogic.game;

import java.io.Serializable;

public enum eGameType implements Serializable {
    BASIC("Basic"),
    ADVANCE("Advance");

    private String name;

    eGameType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
