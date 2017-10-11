package gameLogic.users;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.exceptions.ComputerPlayerException;
import gameLogic.game.board.Board;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;

public class ComputerPlayer extends Player {
    private int boardSize;
    private List<Point> optionalMoves = new LinkedList<>();
    private List<Point> suspectedShipPositions = new LinkedList<>();
    // cells that still need to be checked are false
    private boolean[][] knownCellStateBoard;
    private int minimalShipSizeOnBoard;

    private List<String> movesLog = new LinkedList<>();

    public ComputerPlayer(String playerID, String name, int minimalShipSizeOnBoard) {
        super(playerID, name);
        this.minimalShipSizeOnBoard = minimalShipSizeOnBoard;
        movesLog.add("Computer attack results log:");
    }

    // ============================================ init ============================================
    // we only need to check diagonals that are (minimalShipSizeOnBoard - 1) spaces apart
    // if we start at one of the corners we get the least amount of moves necessary
    private void initOptionalMoves() {
        int currCol = 0;
        int currRow = boardSize - 1;

        while (Math.abs(currRow) < boardSize) {
            addDiagonalToOptionalMovesStartingFrom(currRow, currCol);
            currRow -= minimalShipSizeOnBoard;
        }
    }

    private void addDiagonalToOptionalMovesStartingFrom(int currRow, int currCol) {
        // if initial cell is above the board, run to the first row on the board
        while (currRow < 0) {
            currRow++;
            currCol++;
        }

        while (currRow < boardSize && currCol < boardSize) {
            optionalMoves.add(new Point(currRow, currCol));
            currRow++;
            currCol++;
        }
    }

    // ============================================ setters ============================================
    public void setMyBoard(Board board) {
        this.myBoard = board;
        boardSize = board.getBoardSize();
        knownCellStateBoard = new boolean[boardSize][boardSize];
        initOptionalMoves();
    }

    public void clearMovesLog() {
        movesLog.clear();
        movesLog.add("Computer attack results log:");
    }

    // ============================================ getters ============================================
    public List<String> getMovesLog() {
        return movesLog;
    }

    // ============================================ next Move ============================================
    public BoardCoordinates getNextPositionToAttack() throws ComputerPlayerException {
        Point nextPositionToAttack;
        boolean foundCellToCheck;

        do {
            nextPositionToAttack = getNextMove();
            foundCellToCheck = checkCellAlreadyHit(nextPositionToAttack);
        } while (!foundCellToCheck);

        return pointToCoordinates(nextPositionToAttack);
    }

    private Point getNextMove() throws ComputerPlayerException {
        Point nextMove;

        if (!suspectedShipPositions.isEmpty()) {
            nextMove = getNextPointInList(suspectedShipPositions);
        } else if (!optionalMoves.isEmpty()) {
            nextMove = getNextPointInList(optionalMoves);
        } else {
            throw new ComputerPlayerException("Computer player has run out of moves  :(");
        }

        return nextMove;
    }

    private Point getNextPointInList(List<Point> moveList) {
        Point movePoint;
        movePoint = moveList.get(0);

        if (movePoint != null) {
            moveList.remove(0);
        }

        return movePoint;
    }

    private boolean checkCellAlreadyHit(Point movePoint) {
        return !knownCellStateBoard[(int) movePoint.getX()][(int) movePoint.getY()];
    }

    // ============================================ Other methods ============================================
    public eAttackResult attack(BoardCoordinates position) throws CellNotOnBoardException {
        eAttackResult attackResult = super.attack(position);
        Point pointAttacked = BoardCoordinates.coordinatesToPoint(position);
        removePointFromSuspectedShipPositionsListIfExists(pointAttacked);
        knownCellStateBoard[(int) pointAttacked.getX()][(int) pointAttacked.getY()] = true;

        if (attackResult == eAttackResult.HIT_SHIP || attackResult == eAttackResult.HIT_AND_SUNK_SHIP) {
            hitShip(pointAttacked, attackResult);
        }

        movesLog.add("Attacked cell " + position + ". Result was: " + attackResult.toString());

        return attackResult;
    }

    private void hitShip(Point movePoint, eAttackResult attackResult) {
        Point tempPoint = (Point) movePoint.clone();
        // move temp point to top left of surrounding cells
        tempPoint.x -= 1;
        tempPoint.y -= 1;

        for (int i = 0; i < 8; i++) {
            if (tempPoint.x >= 0 &&
                    tempPoint.x < boardSize &&
                    tempPoint.y >= 0 &&
                    tempPoint.y < boardSize) {
                // if is corner point
                if (i % 2 == 0) {
                    removePointFromSuspectedShipPositionsListIfExists(tempPoint);
                    knownCellStateBoard[tempPoint.x][tempPoint.y] = true;
                } else {
                    if (!knownCellStateBoard[tempPoint.x][tempPoint.y]) {
                        addPointToSuspectedShipPositionsListIfDoesNotExist(tempPoint);
                    }
                }
            }

            if (i < 2) {
                tempPoint.x++;
            } else if (i < 4) {
                tempPoint.y++;
            } else if (i < 6) {
                tempPoint.x--;
            } else {
                tempPoint.y--;
            }
        }

        if (attackResult == eAttackResult.HIT_AND_SUNK_SHIP) {
            sunkAShip();
        }
    }

    // after ship is sunk mark all surrounding point as known and remove from suspected list;
    private void sunkAShip() {
        for (Point point : suspectedShipPositions) {
            knownCellStateBoard[point.x][point.y] = true;
        }

        suspectedShipPositions.clear();
    }

    private void removePointFromSuspectedShipPositionsListIfExists(Point point) {
        int listSize = suspectedShipPositions.size();

        for (int i = 0; i < listSize; i++) {
            if (suspectedShipPositions.get(i).x == point.x &&
                    suspectedShipPositions.get(i).y == point.y) {
                suspectedShipPositions.remove(i);
                break;
            }
        }
    }

    private void addPointToSuspectedShipPositionsListIfDoesNotExist(Point point) {
        if (!suspectedShipPositions.contains(point)) {
            suspectedShipPositions.add(new Point(point));
        }
    }

    private BoardCoordinates pointToCoordinates(Point point) {
        return BoardCoordinates.Parse((int) point.getX(), (int) point.getY());
    }
}