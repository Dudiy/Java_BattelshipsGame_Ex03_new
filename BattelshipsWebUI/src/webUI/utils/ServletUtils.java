package webUI.utils;

import webUI.Constants;
import webUI.ServerManager;

import javax.servlet.ServletContext;

public class ServletUtils {
    public static ServerManager getServerManager(ServletContext servletContext) {
        if (servletContext.getAttribute(Constants.SERVER_MANAGER_ATTRIBUTE_NAME) == null) {
            servletContext.setAttribute(Constants.SERVER_MANAGER_ATTRIBUTE_NAME, new ServerManager());
        }
        return (ServerManager) servletContext.getAttribute(Constants.SERVER_MANAGER_ATTRIBUTE_NAME);
    }
}
