package gameLogic.game.gameObjects.ship;

import gameLogic.game.board.BoardCoordinates;
import java.util.LinkedList;

public class RegularShip extends AbstractShip {

    public RegularShip(ShipType shipType, BoardCoordinates position, eShipDirection direction) {
        super(shipType, position, direction);
        this.hitsRemainingUntilSunk = shipType.getLength();
    }

    @Override
    public LinkedList<BoardCoordinates> getShipCoordinatesList() {
        LinkedList<BoardCoordinates> shipCoordinatesListToReturn = new LinkedList<>();
        BoardCoordinates tempCoordinates = getPosition();

        for (int i = 0 ; i < getLength() ; i++){
            shipCoordinatesListToReturn.add(tempCoordinates);
            tempCoordinates.OffsetRow(getDirection().getxDirection());
            tempCoordinates.offsetCol(getDirection().getyDirection());
        }

        return shipCoordinatesListToReturn;
    }

    // ======================================= setters =======================================
    @Override
    public void setDirection(String direction) throws Exception {
        eShipDirection inputDirection = eShipDirection.valueOf(direction);
        if (inputDirection.isBasicShipDirection()) {
            this.direction = inputDirection;
        } else {
            throw new Exception("direction " + direction + "is invalid for regular ships");
        }
    }

    // ======================================= getters =======================================
    @Override
    public eShipDirection getDirection() {
        return direction;
    }
}
