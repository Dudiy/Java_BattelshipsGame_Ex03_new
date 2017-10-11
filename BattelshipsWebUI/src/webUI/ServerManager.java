package webUI;

import gameLogic.GamesManager;
import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.exceptions.ComputerPlayerException;
import gameLogic.game.ChatMessage;
import gameLogic.game.Game;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;
import gameLogic.game.eGameState;
import gameLogic.users.ComputerPlayer;
import gameLogic.users.Player;
import webUI.utils.SessionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerManager {
    private final GamesManager gamesManager = new GamesManager();
    private final double stepNumBetweenVersion = 0.1;
    private double playersVersion = 0;
    private double gamesVersion = 0;

    // ======================================= Setter =======================================

    // ======================================= Getter =======================================
    public double getPlayersVersion() {
        return playersVersion;
    }

    public double getGamesVersion() {
        return gamesVersion;
    }

    public Map<String, Player> getAllPlayers() {
        return gamesManager.getAllPlayers();
    }

    public Map<Integer, Game> getAllGames() {
        return gamesManager.getAllGames();
    }

    public boolean isPlayerExists(String playerName) {
        return gamesManager.getAllPlayers().containsKey(playerName);
    }

    // ======================================= Players Methods =======================================
    public void addPlayer(String playerName) {
        // TODO remove the player ID
        gamesManager.getAllPlayers().put(playerName, new Player(playerName, playerName));
        this.playersVersion += stepNumBetweenVersion;
    }

    public void removePlayer(String playerName) {
        gamesManager.getAllPlayers().remove(playerName);
        this.playersVersion += stepNumBetweenVersion;
    }

    public Player getPlayerByName(String playerName) {
        return gamesManager.getAllPlayers().get(playerName);
    }

    // ======================================= Games Methods =======================================
    public void addGame(String gameCreatorName, InputStream gameFileContentAsInputStream) throws Exception {
        Player gameCreator = gamesManager.getAllPlayers().get(gameCreatorName);
        // we use gameFileContentAsString for multi use of gameFileContentAsInputStream
        String gameFileContentAsString = convertStringToInputStream(gameFileContentAsInputStream);
        gameFileContentAsInputStream = new ByteArrayInputStream(gameFileContentAsString.getBytes(StandardCharsets.UTF_8.name()));
        // test if gameFile valid
        Game loadedGame = gamesManager.loadGameFile(gameCreator, gameFileContentAsInputStream);
        Player testPlayer = new Player("tempPlayer", "tempPlayer");
        gamesManager.startGame(loadedGame, testPlayer, testPlayer);
        // game file valid, but we need only load the game(not start the game)
        gameFileContentAsInputStream = new ByteArrayInputStream(gameFileContentAsString.getBytes(StandardCharsets.UTF_8.name()));
        Game validLoadedGame = gamesManager.loadGameFile(gameCreator, gameFileContentAsInputStream);
        gamesManager.getAllGames().put(validLoadedGame.getID(), validLoadedGame);
        this.gamesVersion += stepNumBetweenVersion;
    }

    public static String convertStringToInputStream(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    public void joinGame(String gameID, String playerName) throws Exception {
        Game gameToJoin = gamesManager.getGameByID(gameID);
        Player player = gamesManager.getPlayerByName(playerName);
        gamesManager.joinGame(gameToJoin, player);
        gamesVersion += stepNumBetweenVersion;
    }

    public void removeGame(String gameID) {
        gamesManager.getAllGames().remove(Integer.parseInt(gameID));
        this.gamesVersion += stepNumBetweenVersion;
    }

    public Game getGameByID(String activeGameID) {
        return gamesManager.getGameByID(activeGameID);
    }

    // ======================================= Active Games Methods =======================================
    public eAttackResult makeMove(Game activeGame, String positionToAttackAsString) throws CellNotOnBoardException {
        Instant startTime = Instant.now();
        eAttackResult attackResult = null;
        Player activePlayer = activeGame.getActivePlayer();
        BoardCoordinates positionToAttack = BoardCoordinates.Parse(positionToAttackAsString);
        attackResult = gamesManager.makeMove(activeGame, positionToAttack);
        Duration turnTime = Duration.between(startTime, Instant.now());
        activePlayer.addTurnDurationToTotal(turnTime);
        activeGame.updateVersion();
        return attackResult;
        // TODO
//        if (activeGame.getGameState() == eGameState.PLAYER_WON) {
//            onGameEnded(eGameState.STARTED);
//        }
    }

    //TODO delete?
//    private BoardCoordinates parseStringCellPositionToCoordinate(String position) {
//        BoardCoordinates userSelection = null;
//        try {
//            userSelection = BoardCoordinates.Parse(position);
//        } catch (Exception e) {
//            System.out.println("Invalid attack position" + e.getMessage());
//        }
//        return userSelection;
//    }

    public void removePlayer(String gameID, String playerName) throws Exception {
        Game activeGame = getGameByID(gameID);
        Player player = getPlayerByName(playerName);
        activeGame.removePlayerFromGame(player);
        if(activeGame.isAllPlayersRemove()){
//            gamesManager.endGame(activeGame);
            activeGame.resetGame();
            activeGame.updateVersion();
        }
    }
//
    public void endGame(Game activeGame, Player quitter){
        gamesManager.endGame(activeGame, quitter);
//        activeGame.removePlayersFromGame();
//        activeGame.updateVersion();
    }

    public void resetGame(Game activeGame) throws Exception {
        activeGame.resetGame();
        this.gamesVersion += stepNumBetweenVersion;
    }

    public String activeGameVersion(Game activeGame) {
        return null;
    }

    // ======================================= Chat Message Methods =======================================
    public void addChatMessage(Game activeGame, String playerName, String message) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date messageInputTime = new Date();
        activeGame.addNewChatMessage(playerName,message, dateFormat.format(messageInputTime));
    }

    public List<ChatMessage> getNewChatMessage(Game activeGame, int version) {
        return activeGame.getNewChatMessage(version);
    }
}
