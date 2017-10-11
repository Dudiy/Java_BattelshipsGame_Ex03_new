package gameLogic.game.gameObjects.ship;

import gameLogic.game.board.Board;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.gameObjects.GameObject;

import java.util.LinkedList;

public class LShapeShip extends AbstractShip {
    // position is the meeting point of the row and column
    public LShapeShip(ShipType shipType, BoardCoordinates position, eShipDirection direction) {
        super(shipType, position, direction);
        this.hitsRemainingUntilSunk = 2 * shipType.getLength() - 1;
    }
	
	@Override
    public LinkedList<BoardCoordinates> getShipCoordinatesList() {
        LinkedList<BoardCoordinates> shipCoordinatesListToReturn = new LinkedList<>();
        BoardCoordinates tempRowCoordinates = getPosition();
        BoardCoordinates tempColCoordinates = getPosition();

        shipCoordinatesListToReturn.add(getPosition());
        for (int i = 0 ; i < getLength() ; i++){
            tempRowCoordinates.OffsetRow(getDirection().getxDirection());
            tempColCoordinates.offsetCol(getDirection().getyDirection());
            shipCoordinatesListToReturn.add(tempRowCoordinates);
            shipCoordinatesListToReturn.add(tempColCoordinates);
        }

        return shipCoordinatesListToReturn;
    }
	
    // ======================================= setters =======================================
    @Override
    public void setDirection(String direction) throws Exception {
        eShipDirection inputDirection = eShipDirection.valueOf(direction);
        if (!inputDirection.isLShapeShipDirection()) {
            this.direction = inputDirection;
        } else {
            throw new Exception("direction " + direction + "is invalid for L shaped ships");
        }
    }

    // ======================================= getters =======================================
    @Override
    public eShipDirection getDirection() {
        return direction;
    }
//
//    public GameObject clone(){
//    }
}
