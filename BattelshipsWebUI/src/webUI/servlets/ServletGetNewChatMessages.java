package webUI.servlets;

import com.google.gson.Gson;
import gameLogic.game.Game;
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

@WebServlet(name = "ServletGetNewChatMessages", urlPatterns = "/pages/activeGame/getNewChatMessages")
public class ServletGetNewChatMessages extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        final String VERSION = "version";
        final String versionString = request.getParameter(VERSION);
        int version = Integer.parseInt(versionString);
        final String gameID = SessionUtils.getActiveGame(request);
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        Game activeGame = serverManager.getGameByID(gameID);
        try (PrintWriter out = response.getWriter()) {
            String jsonResponse = null;
            Gson gson = new Gson();
            jsonResponse = gson.toJson(serverManager.getNewChatMessage(activeGame, version));
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
}