package webUI.utils;

import webUI.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtils {
    public static String getServerManager(HttpServletRequest request) {
        return getStringAttribute(request, Constants.SERVER_MANAGER_ATTRIBUTE_NAME);
    }

    public static String getPlayerName (HttpServletRequest request) {
        return getStringAttribute(request, Constants.PLAYER_NAME);
    }

    public static String getGameFileToLoad(HttpServletRequest request) {
        return getStringAttribute(request, Constants.GAME_FILE);
    }

    public static String getActiveGame(HttpServletRequest request) {
        return getStringAttribute(request, Constants.ACTIVE_GAME_ID);
    }

    public static void setActiveGame(HttpServletRequest request, String gameID) {
        request.getSession(true).setAttribute(Constants.ACTIVE_GAME_ID, gameID);
    }

    public static void removeGameAttribute(HttpServletRequest request) {
        request.getSession(true).removeAttribute(Constants.ACTIVE_GAME_ID);
    }

    private static String getStringAttribute(HttpServletRequest request, final String attributeName) {
        // false mean if not find return null
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(attributeName) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static void clearSession (HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
