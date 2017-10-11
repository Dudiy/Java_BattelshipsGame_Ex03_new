package gameLogic.exceptions;

public class ComputerPlayerException extends Exception {
    public ComputerPlayerException(String description) {
        super("Computer player exception: " + description);
    }
}
