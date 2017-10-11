package gameLogic.users;

import java.time.Duration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.exceptions.InvalidGameObjectPlacementException;
import gameLogic.exceptions.NoMinesAvailableException;
import gameLogic.game.board.Board;
import gameLogic.game.board.BoardCell;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.gameObjects.Mine;
import gameLogic.game.eAttackResult;
import gameLogic.game.gameObjects.ship.AbstractShip;


public class Player implements User, Serializable {
    private String ID;
    private String name;
    private int score = 0;
    private int timesHit = 0;
    private int timesMissed = 0;
    // duration of a turn is from the time the user selects make move until he enters the cell to attack
    private Duration totalTurnsDuration = Duration.ZERO;
    private int numTurnsPlayed = 0;
    protected Board myBoard;
    protected Board opponentBoard;
    private HashMap<String, Integer> activeShipsOnBoard = new HashMap<>();
    private HashMap<String, Integer> initShipsOnBoard = new HashMap<>();

    public Player(String name) {
        this.ID = name;
        this.name = name;
    }

    public Player(String playerID, String name) {
        this.ID = playerID;
        this.name = name;
    }

    // ======================================= setters =======================================
    public void setActiveShipsOnBoard(HashMap<String, Integer> activeShipsOnBoard) {
        for (Map.Entry<String, Integer> entry : activeShipsOnBoard.entrySet()) {
            this.activeShipsOnBoard.put(entry.getKey(), entry.getValue());
            initShipsOnBoard.put(entry.getKey(), entry.getValue());
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTotalTurnsDuration(Duration totalTurnsDuration) {
        this.totalTurnsDuration = totalTurnsDuration;
    }

    public void setNumTurnsPlayed(int numTurnsPlayed) {
        this.numTurnsPlayed = numTurnsPlayed;
    }

    public void setTimesHit(int timesHit) {
        this.timesHit = timesHit;
    }

    public void setTimesMissed(int timesMissed) {
        this.timesMissed = timesMissed;
    }

    public void setMyBoard(Board board) {
        this.myBoard = board;
    }

    public void setOpponentBoard(Board board) {
        this.opponentBoard = board;
    }

    // ======================================= getters =======================================
    public int getTimesHit() {
        return timesHit;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Board getMyBoard() {
        return myBoard;
    }

    public Duration getTotalTurnsDuration() {
        return totalTurnsDuration;
    }

    public int getNumTurnsPlayed() {
        return numTurnsPlayed;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
    }

    public int getScore() {
        return score;
    }

    public int getTimesMissed() {
        return timesMissed;
    }

    public void addTurnDurationToTotal(Duration turnDuration) {
        totalTurnsDuration = totalTurnsDuration.plus(turnDuration);
    }

    public Duration getAvgTurnDuration() {
        return numTurnsPlayed == 0 ? Duration.ZERO : totalTurnsDuration.dividedBy(numTurnsPlayed);
    }

    public HashMap<String, Integer> getActiveShipsOnBoard() {
        HashMap<String, Integer> clone = new HashMap<>();

        activeShipsOnBoard.entrySet().forEach(entry -> clone.put(entry.getKey(), entry.getValue()));

        return clone;
    }

    public HashMap<String, Integer> getInitShipsOnBoard() {
        return initShipsOnBoard;
    }

    // ======================================= Methods =======================================
    public eAttackResult attack(BoardCoordinates position) throws CellNotOnBoardException {
        eAttackResult attackResult = opponentBoard.attack(position);

        if (attackResult == eAttackResult.HIT_WATER) {
            timesMissed++;
        } else if (attackResult == eAttackResult.HIT_SHIP || attackResult == eAttackResult.HIT_AND_SUNK_SHIP) {
            timesHit++;
        } else if (attackResult == eAttackResult.HIT_MINE) {
            timesHit++;
            myBoard.attack(position);
//            eAttackResult mineExplosionResult = myBoard.attack(position);
//            if (mineExplosionResult == eAttackResult.HIT_MINE) {
//                BoardCell cellHit = opponentBoard.getBoardCellAtCoordinates(position);
////                cellHit.removeGameObjectFromCell();
//            }
        }

        if (attackResult != eAttackResult.CELL_ALREADY_ATTACKED) {
            numTurnsPlayed++;
        }

        return attackResult;
    }

    public void addToScore(int amountToAdd) {
        score += amountToAdd;
    }

    public void plantMine(BoardCoordinates position) throws CellNotOnBoardException, InvalidGameObjectPlacementException, NoMinesAvailableException {
        BoardCell cellToPlantMine = myBoard.getBoardCellAtCoordinates(position);

        if (myBoard.getMinesAvailable() == 0) {
            throw new NoMinesAvailableException();
        }

        if (cellToPlantMine.wasAttacked()) {
            throw new InvalidGameObjectPlacementException(Mine.class.getSimpleName(), position, "Cannot place mine on a cell that was attacked");
        }

        if (myBoard.allSurroundingCellsClear(cellToPlantMine, null)) {
            cellToPlantMine.setCellValue(new Mine(position));
            myBoard.minePlanted();
        } else {
            throw new InvalidGameObjectPlacementException(Mine.class.getSimpleName(), position, "All surrounding cells must be clear.");
        }
    }

    public void OnShipSunk(AbstractShip shipSunk) {
        int shipsRemaining = activeShipsOnBoard.get(shipSunk.getID());
        activeShipsOnBoard.put(shipSunk.getID(), shipsRemaining - 1);
    }

    public void OnShipComeBackToLife(AbstractShip shipWhoComeBackToLife) {
        int shipsRemaining = activeShipsOnBoard.get(shipWhoComeBackToLife.getID());
        activeShipsOnBoard.put(shipWhoComeBackToLife.getID(), shipsRemaining + 1);
    }

    public void resetScores(){
        score = 0;
        timesHit = 0;
        timesMissed = 0;
        totalTurnsDuration = Duration.ZERO;
        numTurnsPlayed = 0;
    }
}
