package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import server.websocket.WebSocketHandler;
import webResult.ClearResult;

/**
 * The web service for application requests
 */
public class ApplicationService {

    private static final ApplicationService applicationService = new ApplicationService();

    public static ApplicationService getInstance() {
        return applicationService;
    }

    private DataAccess dataAccess;
    private WebSocketHandler webSocketHandler;

    public void init(DataAccess dataAccess, WebSocketHandler webSocketHandler) {
        applicationService.dataAccess = dataAccess;
        applicationService.webSocketHandler = webSocketHandler;
    }

    /**
     * Clear the application data (users, games, authentication)
     * @return the clear web result
     */
    public ClearResult clearApplication() {
        try {
            dataAccess.clearData();
            webSocketHandler.clear();
            return new ClearResult(null, true);
        } catch (DataAccessException exception) {
            return new ClearResult("Error: " + exception.getMessage(), false);
        }
    }
}
