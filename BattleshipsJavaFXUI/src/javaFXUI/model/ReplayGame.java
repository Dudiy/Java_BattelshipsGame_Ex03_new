package javaFXUI.model;

import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;
import gameLogic.users.Player;
import java.time.Duration;

public class ReplayGame {
    private Player activePlayer;
    // board
    private BoardCoordinates positionWasAttacked;
    private boolean myBoardCellWasAttacked;
    private boolean opponentsBoardCellWasAttacked;
    // statistic
    private Integer minesAmount;
    private Integer currentScore;
    private Duration totalPlayerTurnsDuration;
    private Integer numPlayerTurnsPlayed;
    private Integer hitNum;
    private Integer missNum;
    private eAttackResult attackResult = null;

    public enum eReplayStatus {
        START_LIST,
        END_LIST,
        PREV,
        NEXT
    }

    public ReplayGame(Player activePlayer, BoardCoordinates positionWasAttacked) throws CellNotOnBoardException {
        this.positionWasAttacked = positionWasAttacked;
        this.myBoardCellWasAttacked = activePlayer.getMyBoard().getBoardCellAtCoordinates(positionWasAttacked).wasAttacked();
        this.opponentsBoardCellWasAttacked = activePlayer.getOpponentBoard().getBoardCellAtCoordinates(positionWasAttacked).wasAttacked();
        this.activePlayer = activePlayer;
        minesAmount = activePlayer.getMyBoard().getMinesAvailable();
        currentScore = activePlayer.getScore();
        totalPlayerTurnsDuration = activePlayer.getTotalTurnsDuration();
        numPlayerTurnsPlayed = activePlayer.getNumTurnsPlayed();
        hitNum = activePlayer.getTimesHit();
        missNum = activePlayer.getTimesMissed();
    }

    // ===================================== Setter =====================================
    public void setAttackResult(eAttackResult attackResult) {
        this.attackResult = attackResult;
    }

    // ===================================== Getter =====================================
    public BoardCoordinates getPositionAttacked() {
        return positionWasAttacked;
    }

    public boolean wasMyBoardCellAttacked() {
        return myBoardCellWasAttacked;
    }

    public boolean wasOpponentsBoardCellAttacked() {
        return opponentsBoardCellWasAttacked;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public Integer getMinesAmount() {
        return minesAmount;
    }

    public Integer getCurrentScore() {
        return currentScore;
    }

    public Duration getTotalPlayerTurnsDuration() {
        return totalPlayerTurnsDuration;
    }

    public Integer getNumPlayerTurnsPlayed() {
        return numPlayerTurnsPlayed;
    }

    public Integer getHitNum() {
        return hitNum;
    }

    public Integer getMissNum() {
        return missNum;
    }

    public eAttackResult getAttackResult() {
        return attackResult;
    }
}
