package gameLogic.game.gameObjects.ship;

import java.io.Serializable;
import jaxb.generated.BattleShipGame;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.GameSettings;

public class ShipFactory implements Serializable {
    private GameSettings gameSettings;

    public ShipFactory(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
    }

    public AbstractShip createShip(BattleShipGame.Boards.Board.Ship ship) throws Exception {
        ShipType shipType = gameSettings.getShipTypesOnBoard().get(ship.getShipTypeId());
        String shipCategory = shipType.getCategory();
        AbstractShip shipObject;
        BoardCoordinates coordinates = BoardCoordinates.convertFromXmlToBoard(ship.getPosition().getX(), ship.getPosition().getY());

        switch (shipCategory) {
            case "REGULAR": {
                eShipDirection direction = eShipDirection.valueOf(ship.getDirection());
                shipObject = new RegularShip(shipType, coordinates, direction);
                break;
            }
            case "L_SHAPE": {
                eShipDirection direction = eShipDirection.valueOf((ship.getDirection()));
                shipObject = new LShapeShip(shipType, coordinates, direction);
                break;
            }
            default:
                throw new Exception("ship factory error - invalid ship category");
        }

        return shipObject;
    }
}
