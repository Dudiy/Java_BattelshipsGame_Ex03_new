package webUI.servlets;

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

@WebServlet(name = "ServletJoinGame", urlPatterns = "/pages/gamesRoom/joinGame")
public class ServletJoinGame extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        if (SessionUtils.getActiveGame(request) == null) {
            Boolean requestSuccess = true;
            try {
                ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
                String gameID = request.getParameter(Constants.GAME_ID);
                String playerName = SessionUtils.getPlayerName(request);
                serverManager.joinGame(gameID, playerName);
                request.getSession(true).setAttribute(Constants.ACTIVE_GAME_ID, gameID);
            } catch (Exception e) {
                requestSuccess = false;
            }
            try (PrintWriter out = response.getWriter()) {
                out.print(requestSuccess.toString());
                out.flush();
            }
        }else{
            throw new ServletException("You are already in a game game");
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

// test