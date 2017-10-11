package gameLogic;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.LoadException;
import gameLogic.exceptions.*;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.Game;
import gameLogic.game.GameSettings;
import gameLogic.game.eGameState;
import gameLogic.users.Player;
import gameLogic.game.eAttackResult;

public class GamesManager implements IGamesLogic {
    private Map<String, Player> allPlayers = new HashMap<>();
    private Map<Integer, Game> allGames = new HashMap<>();

    private void addPlayers(Player[] players) {
        allPlayers.put(players[0].getID(), players[0]);
        allPlayers.put(players[1].getID(), players[1]);
    }

    public Map<String, Player> getAllPlayers() {
        return allPlayers;
    }

    public Map<Integer, Game> getAllGames() {
        return allGames;
    }

    @Override
    public Game loadGameFile(String path) throws LoadException {
        GameSettings gameSettings = GameSettings.loadGameFile(path);
        Game newGame = new Game(gameSettings);
        allGames.put(newGame.getID(), newGame);

        return newGame;
    }

    @Override
    public Game loadGameFile(Player gameCreator, InputStream fileInputStream) throws LoadException {
        GameSettings gameSettings = GameSettings.loadGameFile(fileInputStream);
        Game newGame = new Game(gameCreator, gameSettings);
        // TODO i delete it in Ex3
//        allGames.put(newGame.getID(), newGame);

        return newGame;
    }

    @Override
    public void joinGame(Game game, Player player) throws Exception {
        game.join(player);
        if (game.isGameReadyToStart()) {
            startGame(game);
        }
    }

    @Override
    public void startGame(Game gameToStart, Player player1, Player player2) throws Exception {
        gameToStart.initGame(player1, player2);
        gameToStart.setGameState(eGameState.STARTED);
    }

    // assume there are already two players
    private void startGame(Game gameToStart) throws Exception {
        Player player1 = gameToStart.getPlayers()[0];
        Player player2 = gameToStart.getPlayers()[1];
        if (player1 != null && player2 != null) {
            startGame(gameToStart, player1, player2);
            // setInitialValuesForPlayer
            Player[] players = gameToStart.getPlayers();
            for (Player player : players) {
                player.getMyBoard().setMinesAvailable(2);
                player.setActiveShipsOnBoard(gameToStart.getGameSettings().getShipAmountsOnBoard());
            }
            gameToStart.updateVersion();
        } else {
            // TODO check that case
            throw new Exception("Try start game without initialize all the game players");
        }
    }

    @Override
    public eAttackResult makeMove(Game game, BoardCoordinates cellToAttack) throws CellNotOnBoardException {
        return game.attack(cellToAttack);
    }

    @Override
    public Duration getGameDuration(Game game) {
        return game.getTotalGameDuration();
    }


    @Override
    public void plantMine(Game game, BoardCoordinates cell) throws CellNotOnBoardException, InvalidGameObjectPlacementException, NoMinesAvailableException {
        game.plantMineOnActivePlayersBoard(cell);
    }

    @Override
    public void saveGameToFile(Game game, String fileName) throws Exception {
        Game.saveToFile(game, fileName);
    }

    @Override
    public Game loadSavedGameFromFile(String fileName) throws Exception {
        Game game = Game.loadFromFile(fileName);
        allGames.put(game.getID(), game);
        addPlayers(game.getPlayers());
        game.setGameState(eGameState.STARTED);
        return game;
    }

    @Override
    public void endGame(Game game, Player quitter) {
        if (game.getGameState() == eGameState.STARTED) {
            game.playerForfeit(quitter);
        }
    }

    @Override
    public void endGame(Game game) {

    }

    @Override
    public boolean gameTitleExists(String gameTitleToFind) {
        boolean gameTitleFound = false;

        for (Game game : allGames.values()) {
            if (game.getGameTitle().equals(gameTitleToFind)) {
                gameTitleFound = true;
                break;
            }
        }

        return gameTitleFound;
    }


    public Player getPlayerByName(String playerName) {
        return allPlayers.get(playerName);
    }

    public Game getGameByID(String gameID) {
        return allGames.get(Integer.parseInt(gameID));
    }
}
