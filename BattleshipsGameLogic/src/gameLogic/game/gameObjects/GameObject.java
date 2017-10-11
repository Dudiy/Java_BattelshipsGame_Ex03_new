package gameLogic.game.gameObjects;

import java.io.Serializable;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;

public abstract class GameObject implements Serializable, Cloneable {
    protected static final boolean VISIBLE = true;
    private BoardCoordinates position;
    private boolean isVisible;

    protected GameObject(BoardCoordinates position, boolean isVisible) {
        this.position = position;
        this.isVisible = isVisible;
    }

    // ======================================= setters =======================================
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    // ======================================= getters =======================================
    public BoardCoordinates getPosition() {
        return position.clone();
    }

    public boolean isVisible() {
        return isVisible;
    }

    // ======================================= methods =======================================
    public final eAttackResult attack() {
        isVisible = true;
        return getAttackResult();
    }

    protected abstract eAttackResult getAttackResult();
}
