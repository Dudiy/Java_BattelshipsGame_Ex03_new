package webUI.servlets;

import com.google.gson.Gson;
import gameLogic.game.Game;
import gameLogic.game.eGameState;
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

@WebServlet(name = "ServletAllGames", urlPatterns = "/pages/gamesRoom/ServletAllGames")
public class ServletAllGames extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        List<GameDetails> gamesDetails = new LinkedList<>();

        try (PrintWriter out = response.getWriter()) {
            for (Game game : serverManager.getAllGames().values()) {
                gamesDetails.add(new GameDetails(game, SessionUtils.getPlayerName(request)));
            }

            Gson gson = new Gson();
            double version = serverManager.getGamesVersion();
            String jsonResponse = gson.toJson(new GameAndVersion(version, gamesDetails));
            out.print(jsonResponse);
            out.flush();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private class GameDetails {
        private int gameID;
        private String gameTitle;
        private String creatorName;
        private eGameState gameState;
        private int boardSize;
        private String gameType;
        private String activePlayerFromSession;
        private List<String> activePlayers = new LinkedList<>();

        public GameDetails(Game game, String activePlayerFromSession) {
            gameID = game.getID();
            gameTitle = game.getGameTitle();
            creatorName = game.getGameCreator().getName();
            gameState = game.getGameState();
            boardSize = game.getBoardSize();
            gameType = game.getGameType().toString();
            activePlayers.add(game.getPlayers()[ 0 ] == null ? null : game.getPlayers()[ 0 ].getName());
            activePlayers.add(game.getPlayers()[ 1 ] == null ? null : game.getPlayers()[ 1 ].getName());
            this.activePlayerFromSession = activePlayerFromSession;
        }
    }

    private class GameAndVersion {
        private double gamesVersion;
        private List<GameDetails> gamesDetails;

        public GameAndVersion(double gamesVersion, List<GameDetails> gamesDetails) {
            this.gamesVersion = gamesVersion;
            this.gamesDetails = gamesDetails;
        }
    }
}
