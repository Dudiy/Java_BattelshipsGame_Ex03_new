package javaFXUI.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ShipsStateDataModel {
    private final SimpleStringProperty shipType = new SimpleStringProperty();
    private final SimpleIntegerProperty initialAmount = new SimpleIntegerProperty();
    private final IntegerProperty shipsRemainingActivePlayer = new SimpleIntegerProperty();
    private final IntegerProperty shipsRemainingOtherPlayer = new SimpleIntegerProperty();

    public ShipsStateDataModel(String shipType, int initialAmount) {
        this(shipType,initialAmount,initialAmount, initialAmount);
    }

    public ShipsStateDataModel(String shipType, int initialAmount, int shipsRemainingActivePlayer, int shipsRemainingOtherPlayer) {
        this.shipType.setValue(shipType);
        this.initialAmount.setValue(initialAmount);
        this.shipsRemainingActivePlayer.setValue(shipsRemainingActivePlayer);
        this.shipsRemainingOtherPlayer.setValue(shipsRemainingOtherPlayer);
    }

    public int getShipsRemainingOtherPlayer() {
        return shipsRemainingOtherPlayer.get();
    }

    public IntegerProperty shipsRemainingOtherPlayerProperty() {
        return shipsRemainingOtherPlayer;
    }

    public IntegerProperty shipsRemainingActivePlayerProperty() {
        return shipsRemainingActivePlayer;
    }

    public void setShipsRemainingActivePlayer(int shipsRemainingActivePlayer) {
        this.shipsRemainingActivePlayer.set(shipsRemainingActivePlayer);
    }

    public void setShipsRemainingOtherPlayer(int shipsRemainingOtherPlayer) {
        this.shipsRemainingOtherPlayer.set(shipsRemainingOtherPlayer);
    }

    public String getShipType() {
        return shipType.get();
    }

    public void setShipType(String shipType) {
        this.shipType.set(shipType);
    }

    public int getInitialAmount() {
        return initialAmount.get();
    }

    public void setInitialAmount(int initialAmount) {
        this.initialAmount.set(initialAmount);
    }

    public int getShipsRemainingActivePlayer() {
        return shipsRemainingActivePlayer.get();
    }

    public ShipsStateDataModel clone(){
        return new ShipsStateDataModel(shipType.getValue(),initialAmount.getValue(), shipsRemainingActivePlayer.getValue(), shipsRemainingOtherPlayer.getValue());
    }
}
