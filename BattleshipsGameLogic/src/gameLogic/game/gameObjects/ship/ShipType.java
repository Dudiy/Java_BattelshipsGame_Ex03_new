package gameLogic.game.gameObjects.ship;

import jaxb.generated.BattleShipGame;

import java.io.Serializable;

public class ShipType implements Serializable {
    private String category;
    private int initialAmount;
    private int length;
    private int score;
    private String id;

    public ShipType(BattleShipGame.ShipTypes.ShipType generatedShipType) {
        this.id = generatedShipType.getId();
        this.category = generatedShipType.getCategory();
        this.initialAmount = generatedShipType.getAmount();
        this.length = generatedShipType.getLength();
        this.score = generatedShipType.getScore();
    }

    public String getCategory() {
        return category;
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    public int getLength() {
        return length;
    }

    public int getScore() {
        return score;
    }

    public String getId() {
        return id;
    }
}
