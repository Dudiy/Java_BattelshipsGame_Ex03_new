package gameLogic.game.gameObjects.ship;

import java.awt.*;

public enum eShipDirection {
    ROW(1,0),
    COLUMN(0,1),
    RIGHT_DOWN(-1,1),
    RIGHT_UP(-1,-1),
    UP_RIGHT(1,1),
    DOWN_RIGHT(1,-1);

    private int xDirection;
    private int yDirection;

    eShipDirection(int xDirection, int yDirection) {
        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    public boolean isBasicShipDirection() {
        return this == ROW || this == COLUMN;
    }

    public boolean isLShapeShipDirection() {
        return this == RIGHT_DOWN ||
                this == RIGHT_UP ||
                this == UP_RIGHT ||
                this == DOWN_RIGHT;
    }

    public int getxDirection() {
        return xDirection;
    }

    public int getyDirection() {
        return yDirection;
    }
}
