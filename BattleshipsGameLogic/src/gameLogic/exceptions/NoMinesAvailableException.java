package gameLogic.exceptions;

public class NoMinesAvailableException extends Exception {

    public NoMinesAvailableException() {
        super("No mines available");
    }
}
