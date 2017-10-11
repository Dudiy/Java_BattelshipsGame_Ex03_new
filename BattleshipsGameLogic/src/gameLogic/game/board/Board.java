package gameLogic.game.board;

import java.io.Serializable;
import java.util.LinkedList;

import gameLogic.exceptions.*;
import gameLogic.game.gameObjects.*;
import gameLogic.game.gameObjects.ship.*;
import gameLogic.game.eAttackResult;

public class Board implements Cloneable, Serializable {
    private BoardCell[][] board;
    private final int BOARD_SIZE;
    private LinkedList<AbstractShip> shipsOnBoard = new LinkedList<>();
    private int minesAvailable;

    public Board(int boardSize) {
        this.BOARD_SIZE = boardSize;
        initBoard();
    }

    private void initBoard() {
        board = new BoardCell[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                //ctor of new BoardCell points to a new "Water" object by default
                board[row][col] = new BoardCell((char) ('A' + col), row + 1);
            }
        }
    }

    // ======================================= setters =======================================
    // set the value of the BoardCell at the given coordinates to be "value"
    // throws InvalidGameObjectPlacementException if value cannot be placed in the given coordinates on this board
    private void setCellValue(BoardCoordinates position, GameObject value) throws InvalidGameObjectPlacementException {
        try {
            BoardCell cell = getBoardCellAtCoordinates(position);
            if (value instanceof AbstractShip && !allSurroundingCellsClear(cell, value)) {
                throw new InvalidGameObjectPlacementException(value.getClass().getSimpleName(), position, "Surrounding cells are not clear.");
            } else {
                cell.setCellValue(value);
            }
        } catch (CellNotOnBoardException cellNotOnBoardException) {
            throw new InvalidGameObjectPlacementException(value.getClass().getSimpleName(), position, "cannot place object on the cell because the cell is not on the board");
        }
    }

    public void setMinesAvailable(int minesAvailable) {
        this.minesAvailable = minesAvailable;
    }

    // ======================================= getters =======================================
    public int getMinesAvailable() {
        return minesAvailable;
    }

    public BoardCell[][] getBoard() {
        return board;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    private BoardCell getCellByOffset(BoardCell srcCell, eBoardDirection direction, int offset) throws CellNotOnBoardException {
        BoardCoordinates requiredCoordinates = new BoardCoordinates(srcCell.getPosition());

        switch (direction) {
            case DOWN:
                requiredCoordinates.OffsetRow(offset);
                break;
            case UP:
                requiredCoordinates.OffsetRow(-offset);
                break;
            case RIGHT:
                requiredCoordinates.offsetCol(offset);
                break;
            case LEFT:
                requiredCoordinates.offsetCol(-offset);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction entered");
        }

        return getBoardCellAtCoordinates(requiredCoordinates);
    }

    // return a the BoardCell object on this board that is at the given coordinates
    public BoardCell getBoardCellAtCoordinates(BoardCoordinates coordinates) throws CellNotOnBoardException {
        BoardCell res;

        if (coordinatesAreOnBoard(coordinates)) {
            int col = coordinates.getColIndexInMemory();
            int row = coordinates.getRowIndexInMemory();
            res = board[row][col];
        } else {
            throw new CellNotOnBoardException();
        }

        return res;
    }

    // ======================================= methods =======================================
    public boolean allSurroundingCellsClear(BoardCell cell, GameObject objectBeingCheckedFor) {
        BoardCoordinates tempPosition = new BoardCoordinates(cell.getPosition());
        boolean allClear = true;

        //start from top left cell
        tempPosition.OffsetRow(-1);
        tempPosition.offsetCol(-1);
        for (int i = 0; i < 8; i++) {
            try {
                GameObject objectAtCell = this.getBoardCellAtCoordinates(tempPosition).getCellValue();
                if (!(objectAtCell instanceof Water) && (objectAtCell != objectBeingCheckedFor)) {
                    allClear = false;
                    break;
                }
            } catch (CellNotOnBoardException e) {
                // the given cell is on one of the edges, error while trying to fetch a surrounding cell not on the board
            } finally {
                // move 2 right
                if (i < 2) {
                    tempPosition.offsetCol(1);
                }
                // move 2 down
                else if (i < 4) {
                    tempPosition.OffsetRow(1);
                }
                // move 2 left
                else if (i < 6) {
                    tempPosition.offsetCol(-1);
                }
                // move 2 up
                else {
                    tempPosition.OffsetRow(-1);
                }
            }
        }

        return allClear;
    }

    // add a new ship to the board
    public void addShipToBoard(AbstractShip ship) throws Exception {
        if (ship instanceof RegularShip) {
            addRegularShipToBoard((RegularShip) ship);
        } else if (ship instanceof LShapeShip) {
            addLShapeShipToBoard((LShapeShip) ship);
        } else {
            throw new IllegalArgumentException("The ship type given is not supported");
        }
        // if we get here then there was no exception thrown => ship was added
        shipsOnBoard.add(ship);
    }

    // add a new RegularShip to this board
    private void addRegularShipToBoard(RegularShip ship) throws Exception {
        BoardCoordinates currCoordinates = ship.getPosition();
        eShipDirection shipDirection = ship.getDirection();

        for (int i = 0; i < ship.getLength(); i++) {
            setCellValue(currCoordinates, ship);
            if (shipDirection == eShipDirection.COLUMN) {
                currCoordinates.OffsetRow(1);
            } else if (shipDirection == eShipDirection.ROW) {
                currCoordinates.offsetCol(1);
            } else {
                throw new IllegalArgumentException("The given ship has an unknown direction value");
            }
        }
    }

    public boolean allShipsWereSunk() {
        boolean allShipsWereSunk = true;

        for (AbstractShip ship : shipsOnBoard) {
            if (ship.getHitsRemainingUntilSunk() != 0) {
                allShipsWereSunk = false;
                break;
            }
        }

        return allShipsWereSunk;
    }

    // add a new LShapeShip to this board
    private void addLShapeShipToBoard(LShapeShip ship) throws InvalidGameObjectPlacementException {
        BoardCoordinates rowCoordinates = ship.getPosition();
        BoardCoordinates colCoordinates = ship.getPosition();
        eShipDirection shipDirection = ship.getDirection();
		//set the values of the corner
        setCellValue(rowCoordinates, ship);
        // set the values of row
        for (int i = 1; i < ship.getLength(); i++) {
            rowCoordinates.OffsetRow(shipDirection.getxDirection());
            colCoordinates.offsetCol(shipDirection.getyDirection());
            setCellValue(rowCoordinates, ship);
            setCellValue(colCoordinates, ship);
        }
    }

    // check if the given coordinates are on this board
    private boolean coordinatesAreOnBoard(BoardCoordinates coordinates) {
        int col = coordinates.getColIndexInMemory();
        int row = coordinates.getRowIndexInMemory();
        return ((0 <= col && col <= BOARD_SIZE - 1) && (0 <= row && row <= BOARD_SIZE - 1));
    }

    public void minePlanted() {
        minesAvailable--;
    }

    public eAttackResult attack(BoardCoordinates coordinatesToAttack) throws CellNotOnBoardException {
        return getBoardCellAtCoordinates(coordinatesToAttack).attack();
    }
}
