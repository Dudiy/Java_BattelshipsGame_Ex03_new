package webUI.servlets;

import com.google.gson.Gson;
import gameLogic.users.Player;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ServletOnlinePlayers", urlPatterns = "/pages/gamesRoom/onlinePlayers")
public class ServletOnlinePlayers extends HttpServlet {
    private final String TYPE_INFO = "typeInfo";
    private final String CURR_PLAYER_NAME = "currPlayerName";
    private final String ALL_PLAYERS_NAME = "allPlayersName";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        List<String> playersName =new ArrayList<>();
        final String typeInfo = request.getParameter(TYPE_INFO);

        try (PrintWriter out = response.getWriter()) {
            if (typeInfo.equals(CURR_PLAYER_NAME)) {
                playersName.add(SessionUtils.getPlayerName(request));
            } else if (typeInfo.equals(ALL_PLAYERS_NAME)) {
                for (Player player : serverManager.getAllPlayers().values()) {
                    playersName.add(player.getName());
                }
            }
            Gson gson = new Gson();
            double version = serverManager.getPlayersVersion();
            String jsonResponse = gson.toJson(new PlayerAndVersion(version,playersName));
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

    private class PlayerAndVersion{
        private double playersVersion;
        private List<String> playersName;

        public PlayerAndVersion(double playersVersion, List<String> playersName) {
            this.playersVersion = playersVersion;
            this.playersName = playersName;
        }
    }
}