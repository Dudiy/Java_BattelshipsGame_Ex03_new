package gameLogic.game.board;

import java.io.Serializable;

import gameLogic.exceptions.InvalidGameObjectPlacementException;
import gameLogic.game.gameObjects.GameObject;
import gameLogic.game.gameObjects.Water;
import gameLogic.game.eAttackResult;
import gameLogic.game.gameObjects.ship.AbstractShip;

public class BoardCell implements Serializable, Cloneable {
    private BoardCoordinates position;
    private GameObject cellValue;
    private boolean wasAttacked = false;

    public BoardCell() {
    }

    public BoardCell(char col, int row) {
        position = new BoardCoordinates(col, row);
        cellValue = new Water(position);
    }

    // ======================================= setters =======================================

    public void setPosition(BoardCoordinates position) {
        this.position = position;
    }

    public void setCellValue(GameObject cellValue) throws InvalidGameObjectPlacementException {
        if (this.cellValue == null || this.cellValue instanceof Water) {
            this.cellValue = cellValue;
        } else {
            String objectTypeInCell = this.cellValue.getClass().getSimpleName();
            throw new InvalidGameObjectPlacementException(cellValue.getClass().getSimpleName(), position,
                    "Cannot place a game object, cell is already occupied by a " + objectTypeInCell + " object");
        }
    }

    public void setWasAttacked(boolean wasAttacked) {
        this.wasAttacked = wasAttacked;
    }

    // ======================================= getters =======================================
    public BoardCoordinates getPosition() {
        return position.clone();
    }

    public GameObject getCellValue() {
        return cellValue;
    }

    public boolean wasAttacked() {
        return wasAttacked;
    }

    // ======================================= methods =======================================
    // sets the cell value to be water
    public void removeGameObjectFromCell() {
        if (!(cellValue instanceof Water)) {
            this.cellValue = new Water(this.cellValue.getPosition());
        }
    }

    eAttackResult attack() {
        eAttackResult attackResult;
        if (!wasAttacked) {
            attackResult = cellValue.attack();
            wasAttacked = true;
        } else {
            attackResult = eAttackResult.CELL_ALREADY_ATTACKED;
        }
        return attackResult;
    }
}