package webUI.servlets;

import com.google.gson.Gson;
import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.exceptions.InvalidGameObjectPlacementException;
import gameLogic.exceptions.NoMinesAvailableException;
import gameLogic.game.Game;
import gameLogic.game.board.Board;
import gameLogic.game.board.BoardCell;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.gameObjects.GameObject;
import gameLogic.game.gameObjects.Mine;
import gameLogic.game.gameObjects.Water;
import gameLogic.game.gameObjects.ship.AbstractShip;
import gameLogic.users.Player;
import webUI.Constants;
import webUI.ServerManager;
import webUI.utils.ServletUtils;
import webUI.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "ServletActiveGame", urlPatterns = "/pages/activeGame/activeGame")
public class ServletActiveGame extends HttpServlet {
    private static final String ACTIVE_PLAYER_NAME = "activePlayer";
    private static final String GAME_STATE = "gameState";
    private static final String GAME_DETAILS = "gameDetails";
    private static final String MAKE_MOVE = "makeMove";
    private static final String END_GAME = "endGame";
    private static final String RESET_GAME = "resetGame";
    private static final String VERSION = "version";
    private static final String GAME_TYPE = "gameType";
    private static final String PLAYERS_SCORE = "playersScore";
    private static final String CHECK_WIN = "checkWin";
    private static final String PLANT_MINE = "plantMine";
    private Gson gson = new Gson();
    private HttpServletRequest request;

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.request = request;
        response.setContentType("application/json;charset=UTF-8");
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        String activeGameID = SessionUtils.getActiveGame(request);
        Game activeGame = serverManager.getGameByID(activeGameID);

        try (PrintWriter out = response.getWriter()) {
            out.print(getJsonResponse(request, activeGame));
            out.flush();
        }
    }

    private String getJsonResponse(HttpServletRequest request, Game activeGame) throws ServletException {
        String action = request.getParameter(Constants.ACTION);
        String jsonResponse = "{}";
        try {
            switch (action) {
                case ACTIVE_PLAYER_NAME:
                    jsonResponse = getActivePlayerName(activeGame);
                    break;
                case GAME_STATE:
                    jsonResponse = gson.toJson(activeGame.getGameState());
                    break;
                case GAME_DETAILS:
                    jsonResponse = getGameDetails(activeGame);
                    if (jsonResponse.equals("")) {
                        jsonResponse = null;
                    }
                    break;
                case MAKE_MOVE:
                    jsonResponse = gson.toJson(makeMove(activeGame));
                    break;
                case END_GAME:
                    jsonResponse = endGame(request, activeGame);
                    if (jsonResponse.equals("")) {
                        jsonResponse = null;
                    }
                    break;
                case RESET_GAME:
                    resetGame(request, activeGame);
                    break;
                case VERSION:
                    jsonResponse = gson.toJson(activeGame.getGameVersion());
                    break;
                case GAME_TYPE:
                    jsonResponse = gson.toJson(activeGame.getGameSettings().getGameType().toString());
                    break;
                case PLAYERS_SCORE:
                    jsonResponse = getPlayersScore(activeGame);
                    break;
                case CHECK_WIN:
                    jsonResponse = checkWin(activeGame);
                    break;
                case PLANT_MINE:
                    String targetCell = request.getParameter("targetCell");
                    jsonResponse = plantMine(activeGame, targetCell);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

        return jsonResponse;
    }

    private String plantMine(Game activeGame, String targetCell) {
        String jsonResponse = "{ ";

        try {
            activeGame.plantMineOnActivePlayersBoard(BoardCoordinates.Parse(targetCell));
            jsonResponse += "\"minePlanted\": \"true\"";
        } catch (CellNotOnBoardException e) {
            jsonResponse += "\"minePlanted\": \"false\"";
            jsonResponse += ", \"errorMessage\": \"The given cell is not on the board\"";
        } catch (InvalidGameObjectPlacementException e) {
            jsonResponse += "\"minePlanted\": \"false\"";
            jsonResponse += ", \"errorMessage\": \"A mine can not e placed at this position\"";
        } catch (NoMinesAvailableException e) {
            jsonResponse += "\"minePlanted\": \"false\"";
            jsonResponse += ", \"errorMessage\": \"Sorry, you have no mines available\"";
        }
        jsonResponse += " }";
        activeGame.updateVersion();

        return jsonResponse;
    }

    // ======================================= Players Status Methods =======================================
    private String getActivePlayerName(Game activeGame) {
        String currentPlayerNameFromSession = SessionUtils.getPlayerName(request);
        String activePlayerName = activeGame.getActivePlayer() != null ? activeGame.getActivePlayer().getName() : "";
        int numPlayersInGame = 0;
        Player[] players = activeGame.getPlayers();
        if (players[0] != null) {
            numPlayersInGame++;
        }
        if (players[1] != null) {
            numPlayersInGame++;
        }
        // jsonResponse
        return gson.toJson(new GamePlayers(currentPlayerNameFromSession, activePlayerName, numPlayersInGame));
    }

    private class GamePlayers {
        private String currentPlayer;
        private String activePlayer;
        private int numPlayersInGame;

        GamePlayers(String currentPlayer, String activePlayer, int numPlayersInGame) {
            this.currentPlayer = currentPlayer;
            this.activePlayer = activePlayer;
            this.numPlayersInGame = numPlayersInGame;
        }
    }

    // ======================================= Game Details Methods =======================================
    private String getGameDetails(Game activeGame) throws CellNotOnBoardException {
        GameDetails gameDetails;
        if (activeGame.isGameReadyToStart()) {
            ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
            String currentPlayerName = SessionUtils.getPlayerName(request);
            Player currentPlayer = serverManager.getPlayerByName(currentPlayerName);
            Player otherPlayer;
            otherPlayer = currentPlayerName.equals(activeGame.getActivePlayer().getName()) ?
                    activeGame.getOtherPlayer() : activeGame.getActivePlayer();
            Board myBoard = currentPlayer.getMyBoard();
            BoardView myBoardView = myBoard == null ? null : new BoardView(myBoard, true);
            Board opponentBoard = currentPlayer.getOpponentBoard();
            BoardView opponentBoardView = opponentBoard == null ? null : new BoardView(opponentBoard, false);
            GameStatistics gameStatistic = new GameStatistics(currentPlayer, otherPlayer);
            gameDetails = new GameDetails(myBoardView, opponentBoardView, gameStatistic);
        } else {
            gameDetails = new GameDetails(activeGame.getGameSettings().getBoardSize());
        }
        return gson.toJson(gameDetails);
    }

    private static class BoardView {
        private final int boardSize;
        private String[][] boardView;

        private enum eCellState {
            WATER("water"),
            SHIP("ship"),
            SINKING_SHIP("sinking-ship"),
            MINE("mine"),
            HIT("hit"),
            MISS("miss"),
            PROBLEM("problem");

            private String description;

            eCellState(String description) {
                this.description = description;
            }

            @Override
            public String toString() {
                return description;
            }
        }

        // before game start
        public BoardView(int boardSize) {
            this.boardSize = boardSize;
            boardView = new String[boardSize][boardSize];
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    boardView[row][col] = eCellState.WATER.toString();
                }
            }
        }

        BoardView(Board board, boolean boardVisible) throws CellNotOnBoardException {
            boardSize = board.getBoardSize();
            boardView = new String[boardSize][boardSize];
            setBoardView(board, boardVisible);
        }

        private void setBoardView(Board board, boolean boardVisible) throws CellNotOnBoardException {
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    BoardCell boardCell = board.getBoardCellAtCoordinates(BoardCoordinates.Parse(row, col));
                    boardView[row][col] = getCellView(boardCell, boardVisible);
                }
            }
        }

        private String getCellView(BoardCell boardCell, boolean boardVisible) {
            eCellState cellState;
            GameObject cellValue = boardCell.getCellValue();

            if (boardCell.wasAttacked()) {
                if (cellValue instanceof Water) {
                    cellState = eCellState.MISS;
                } else if (cellValue instanceof Mine || cellValue instanceof AbstractShip) {
                    if (cellValue instanceof AbstractShip && ((AbstractShip) cellValue).isSunk()) {
                        cellState = eCellState.SINKING_SHIP;
                    } else {
                        cellState = eCellState.HIT;
                    }
                } else {
                    cellState = eCellState.PROBLEM;
                }
            } else {
                if (cellValue instanceof AbstractShip) {
                    cellState = boardVisible ? eCellState.SHIP : eCellState.WATER;
                } else if (cellValue instanceof Water) {
                    cellState = eCellState.WATER;
                } else if (cellValue instanceof Mine) {
                    cellState = boardVisible ? eCellState.MINE : eCellState.WATER;
                } else {
                    cellState = eCellState.PROBLEM;
                }
            }
            return cellState.toString();
        }
    }

    private class GameStatistics {
        private int minesRemaining;
        private int totalMoveCounter;
        private int myScore;
        private int opponentScore;
        private String averageTurnDuration;
        private int hitCounter;
        private int missCounter;
        private List<ShipState> allShipsState = new ArrayList<>();

        GameStatistics(Player currentPlayer, Player otherPlayer) {
            minesRemaining = currentPlayer.getMyBoard().getMinesAvailable();
            totalMoveCounter = currentPlayer.getNumTurnsPlayed();
            myScore = currentPlayer.getScore();
            opponentScore = otherPlayer != null ? otherPlayer.getScore() : 0;
            averageTurnDuration = currentPlayer.getAvgTurnDuration().toString();
            hitCounter = currentPlayer.getTimesHit();
            missCounter = currentPlayer.getTimesMissed();
            setAllShipsState(currentPlayer, otherPlayer);
        }

        private void setAllShipsState(Player currentPlayer, Player otherPlayer) {
            HashMap<String, Integer> activeShipsOnMyBoard = currentPlayer.getActiveShipsOnBoard();
            HashMap<String, Integer> activeShipsOnOpponentBoard = otherPlayer != null ? otherPlayer.getActiveShipsOnBoard() : null;
            HashMap<String, Integer> initShipsOnBoard = currentPlayer.getInitShipsOnBoard();
            ShipState shipState;
            for (Map.Entry<String, Integer> entry : activeShipsOnMyBoard.entrySet()) {
                shipState = new ShipState();
                String shipType = entry.getKey();
                shipState.setShipType(shipType);
                shipState.setInitAmount(initShipsOnBoard.get(shipType));
                shipState.setMyAmount(entry.getValue());
                shipState.setOpponentAmount(activeShipsOnOpponentBoard != null ? activeShipsOnOpponentBoard.get(shipType) : 0);
                allShipsState.add(shipState);
            }
        }
    }

    private class ShipState {
        private String shipType;
        private int initAmount;
        private int myAmount;
        private int opponentAmount;

        ShipState() {
        }

        void setShipType(String shipType) {
            this.shipType = shipType;
        }

        void setInitAmount(int initAmount) {
            this.initAmount = initAmount;
        }

        void setMyAmount(int myAmount) {
            this.myAmount = myAmount;
        }

        void setOpponentAmount(int opponentAmount) {
            this.opponentAmount = opponentAmount;
        }
    }

    private class GameDetails {
        private BoardView myBoard;
        private BoardView opponentBoard;
        private GameStatistics gameStatistics;

        GameDetails(BoardView myBoard, BoardView opponentBoard, GameStatistics gameStatistics) {
            this.myBoard = myBoard;
            this.opponentBoard = opponentBoard;
            this.gameStatistics = gameStatistics;
        }

        // before game start
        public GameDetails(int boardSize) {
            myBoard = new BoardView(boardSize);
            opponentBoard = new BoardView(boardSize);
        }
    }

    // ======================================= Make Move Methods =======================================
    private String makeMove(Game activeGame) throws CellNotOnBoardException {
        final String POSITION = "cellcoordinate";
        String cellCoordinateAsString = request.getParameter(POSITION);
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        return serverManager.makeMove(activeGame, cellCoordinateAsString).toString();
    }

    // ======================================= End Game Methods =======================================
//    private void endGame(HttpServletRequest request) throws Exception {
//        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
//        String activeGameID = SessionUtils.getActiveGame(request);
//        String currentPlayerName = SessionUtils.getPlayerName(request);
////        serverManager.endGame(activeGameID);
//        serverManager.removePlayer(activeGameID, currentPlayerName);
//        SessionUtils.removeGameAttribute(request);
//    }

    private void removeActivePlayerFromGame(HttpServletRequest request, Game activeGame) throws Exception {
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        String activeGameID = SessionUtils.getActiveGame(request);
        String currentPlayerName = SessionUtils.getPlayerName(request);
        serverManager.removePlayer(activeGameID, currentPlayerName);
    }

    private String endGame(HttpServletRequest request, Game activeGame) {
        String returnValue = "";
        if (activeGame.getActivePlayer() != null && activeGame.getOtherPlayer() != null) {
            ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
            String currentPlayerName = SessionUtils.getPlayerName(request);
            Player currentPlayer = activeGame.getActivePlayer().getName() == currentPlayerName ?
                    activeGame.getActivePlayer() : activeGame.getOtherPlayer();

            returnValue = "{ ";
            serverManager.endGame(activeGame, currentPlayer);
            returnValue += "\"winner\": \"" + activeGame.getWinnerPlayer().getName() + "\"";
            returnValue += ", \"scores\": " + gson.toJson(activeGame.getPlayersScore());
            returnValue += "}";
        }

        return returnValue;
    }

    private void resetGame(HttpServletRequest request, Game activeGame) throws Exception {
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        removeActivePlayerFromGame(request, activeGame);
        SessionUtils.removeGameAttribute(request);
        Player[] players = activeGame.getPlayers();
        // if the other player has already left the game
        if (players[0] == null && players[1] == null) {
            serverManager.resetGame(activeGame);
        }
    }

    // ======================================= Other Methods =======================================
    private String getPlayersScore(Game activeGame) {
//        HashMap<String, String> mapResponse = new HashMap<>();
//        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
//        String currentPlayerNameFromSession = SessionUtils.getPlayerName(request);
//        Player currentPlayer = serverManager.getPlayerByName(currentPlayerNameFromSession);
//        Player otherPlayer = currentPlayerNameFromSession.equals(activeGame.getActivePlayer().getName()) ?
//                activeGame.getOtherPlayer() : activeGame.getActivePlayer();
//
//        mapResponse.put("currentPlayerName", currentPlayerNameFromSession);
//        mapResponse.put("otherPlayerName", currentPlayerNameFromSession);
//        mapResponse.put("currentPlayerScore", Integer.toString(currentPlayer.getScore()));
//        mapResponse.put("otherPlayerScore", Integer.toString(otherPlayer.getScore()));
//         jsonResponse
//        return gson.toJson(mapResponse);
        return gson.toJson(activeGame.getPlayersScore());
    }

    private String checkWin(Game activeGame) {
        String currentPlayerNameFromSession = SessionUtils.getPlayerName(request);
        String jsonResponse;
        if (currentPlayerNameFromSession.equals(activeGame.getWinnerPlayer().getName())) {
            jsonResponse = gson.toJson(true);
        } else {
            jsonResponse = gson.toJson(false);
        }
        return jsonResponse;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
}
