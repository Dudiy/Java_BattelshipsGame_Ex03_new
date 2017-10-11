package webUI.servlets;

import com.google.gson.Gson;
import webUI.ServerManager;
import webUI.utils.ServletUtils;
import webUI.utils.SessionUtils;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "ServletAddGame", urlPatterns = "/pages/gamesRoom/addGameFromFile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class ServletAddGame extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        boolean gameFileValid = true;
        ServerManager serverManager = ServletUtils.getServerManager(getServletContext());
        String gameTitle = request.getParameter("gameTitle");
        String addGameResult = "";

        try (PrintWriter out = response.getWriter()) {
            if (!serverManager.gameTitleExists(gameTitle) && !gameTitle.equals("null") && !gameTitle.equals("")) {
                Collection<Part> parts = request.getParts();
                List<InputStream> inputStreamsOfGameFile = new LinkedList<>();

                for (Part part : parts) {
                    if (part.getName().equals("file")) {
                        inputStreamsOfGameFile.add(part.getInputStream());
                    }
                }

                InputStream gameFileContentAsInputStream = new SequenceInputStream(Collections.enumeration(inputStreamsOfGameFile));
                String gameCreatorName = SessionUtils.getPlayerName(request);
                if (gameCreatorName != null) {
                    try {
                        serverManager.addGame(gameCreatorName, gameTitle, gameFileContentAsInputStream);
                        addGameResult = "Game loaded successfully !";
                    } catch (Exception e) {
                        gameFileValid = false;
                        addGameResult = "Could not load the game, invalid game file selected. inner exception: " + e.getMessage();
                    }
                } else {
                    throw new NullPointerException("While adding new game we get to null game creator name");
                }
            } else {
                gameFileValid = false;
                if (gameTitle.equals("null") || gameTitle.equals("")) {
                    addGameResult = "Could not load the game, A game must have a valid title";
                } else {
                    addGameResult = "Could not load the game, A game with the title \"" + gameTitle + "\" already exists";
                }
            }

            Gson gson = new Gson();
            String jsonResponse = gson.toJson(new GameFile(addGameResult, gameFileValid));
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

    private class GameFile {
        private String loadResult;
        private boolean validFile;

        public GameFile(String loadResult, boolean validFile) {
            this.loadResult = loadResult;
            this.validFile = validFile;
        }
    }
}