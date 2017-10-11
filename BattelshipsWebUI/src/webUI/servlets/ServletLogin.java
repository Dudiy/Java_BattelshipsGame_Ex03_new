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

//TODO change name
@WebServlet(name = "ServletLogin", urlPatterns = {"/pages/login/login"})
public class ServletLogin extends HttpServlet {

    private final String LOGIN_URL = "../login/login.html";
    private final String START_GAME_URL = "../gamesRoom/gamesRoom.html";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        String playerNameFromParameter = request.getParameter(Constants.PLAYER_NAME) != null ? request.getParameter(Constants.PLAYER_NAME).trim() : "";
        try (PrintWriter out = response.getWriter()) {
            String actionType = request.getParameter(Constants.ACTION);
            if (actionType.equals("login")) {
                loginAction(request, serverManager, playerNameFromParameter, out);
            } else if (actionType.equals("logout")) {
                serverManager.removePlayer(request.getSession(true).getAttribute(Constants.PLAYER_NAME).toString());
                request.getSession().setAttribute(Constants.PLAYER_NAME, null);
            } else {
                throw new ServletException("action parameter required");
            }
        }
    }

    private void loginAction(HttpServletRequest request, ServerManager serverManager, String playerNameFromParameter, PrintWriter out) {
        // first time player logged in
        if (request.getSession(true).getAttribute(Constants.PLAYER_NAME) == null) {
            if (playerNameFromParameter.equals("")) {
                out.print("no user name entered");
            } else {
                if (serverManager.isPlayerExists(playerNameFromParameter)) {
                    // player name already exists
                    String errorMessage = "Player name " + playerNameFromParameter + " already exists. Please enter a different name.";
                    out.print(errorMessage);
                } else {
                    //add the new player to the players list
                    serverManager.addPlayer(playerNameFromParameter);
                    //set the username in a session so it will be available on each request
                    //the true parameter means that if a session object does not exists yet create a new one
                    request.getSession(true).setAttribute(Constants.PLAYER_NAME, playerNameFromParameter);
                    //redirect the request to the chat room - in order to actually change the URL
                    out.print("login successful");
                }
            }
        } else {
            //user is already logged in
            String loggedInUser = request.getSession(true).getAttribute(Constants.PLAYER_NAME).toString();
            out.print("already logged in as " + loggedInUser + "\nContinue as " + loggedInUser + "?");
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
