package webUI.servlets;

import com.google.gson.Gson;
import gameLogic.game.Game;
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

@WebServlet(name = "ServletSendChatMessage", urlPatterns = "/pages/activeGame/sendChatMessage")
public class ServletSendChatMessage extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json;charset=UTF-8");
        final String MESSAGE = "message";
        final String gameID = SessionUtils.getActiveGame(request);
        final String playerName = SessionUtils.getPlayerName(request);
        final String message = request.getParameter(MESSAGE);
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        Game activeGame = serverManager.getGameByID(gameID);
        if (message != null && !message.isEmpty()) {
            serverManager.addChatMessage(activeGame, playerName, message);
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
}
