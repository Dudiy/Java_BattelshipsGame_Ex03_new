package gameLogic.game.gameObjects.ship;

import com.sun.jmx.snmp.Enumerated;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.gameObjects.GameObject;
import gameLogic.game.eAttackResult;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractShip extends GameObject {
    private ShipType shipType;
    protected int hitsRemainingUntilSunk;
    eShipDirection direction;

    AbstractShip(ShipType shipType, BoardCoordinates position, eShipDirection direction) {
        super(position, !VISIBLE);
        this.shipType = shipType;
        this.direction = direction;
    }

    public abstract LinkedList<BoardCoordinates> getShipCoordinatesList();

    protected abstract void setDirection(String direction) throws Exception;

    public void increaseHitsRemainingUntilSunk() {
        this.hitsRemainingUntilSunk++;
    }

    public void decreaseHitsRemainingUntilSunk() {
        this.hitsRemainingUntilSunk--;
    }
// ======================================= getters =======================================

    public int getLength() {
        return shipType.getLength();
    }

    public int getScore() {
        return shipType.getScore();
    }

    public String getID() {
        return shipType.getId();
    }

    public abstract eShipDirection getDirection();

    public int getHitsRemainingUntilSunk() {
        return hitsRemainingUntilSunk;
    }

    public boolean isSunk() {
        return hitsRemainingUntilSunk == 0;
    }

    // ======================================= methods =======================================
    @Override
    public eAttackResult getAttackResult() {
        eAttackResult attackResult;

        hitsRemainingUntilSunk--;
        if (hitsRemainingUntilSunk == 0) {
            attackResult = eAttackResult.HIT_AND_SUNK_SHIP;
        } else if (hitsRemainingUntilSunk > 0) {
            attackResult = eAttackResult.HIT_SHIP;
        } else {
            throw new ValueException("Hits remaining until sunk can't be negative");
        }

        return attackResult;
    }
}
