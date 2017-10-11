package gameLogic;

import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.exceptions.InvalidGameObjectPlacementException;
import gameLogic.exceptions.NoMinesAvailableException;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.Game;
import gameLogic.users.Player;
import gameLogic.game.eAttackResult;
import javafx.application.Platform;
import javafx.fxml.LoadException;

import java.io.InputStream;
import java.time.Duration;

public interface IGamesLogic {
    Game loadGameFile(String path) throws LoadException;

    Game loadGameFile(Player gameCreator, InputStream fileInputStream) throws LoadException;

    void joinGame(Game game, Player player) throws Exception;

    void startGame(Game gameToStart, Player player1, Player player2) throws Exception;

//    void startGame(Game gameToStart) throws Exception;

    eAttackResult makeMove(Game game, BoardCoordinates cellToAttack) throws CellNotOnBoardException;

    Duration getGameDuration(Game game);

    void plantMine(Game game, BoardCoordinates cell) throws CellNotOnBoardException, InvalidGameObjectPlacementException, NoMinesAvailableException;

    void saveGameToFile(Game game, final String fileName) throws Exception;

    Game loadSavedGameFromFile(final String fileName) throws Exception;

    void endGame(Game game, Player quitter);

    void endGame(Game game);

    boolean gameTitleExists(String gameTitle);
}
