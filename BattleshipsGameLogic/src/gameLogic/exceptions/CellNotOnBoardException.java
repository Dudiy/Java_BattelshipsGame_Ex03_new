package gameLogic.exceptions;

public class CellNotOnBoardException extends Exception{
    public CellNotOnBoardException() {
        super("The cell value is not on the board");
    }
}
