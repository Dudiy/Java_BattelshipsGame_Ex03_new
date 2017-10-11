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

@WebServlet(name = "ServletDeleteGame", urlPatterns = "/pages/gamesRoom/deleteGame")
public class ServletDeleteGame extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        String gameID = request.getParameter(Constants.GAME_ID);
        serverManager.removeGame(gameID);
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
