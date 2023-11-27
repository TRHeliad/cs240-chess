package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
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

    public void init(DataAccess dataAccess) {
        applicationService.dataAccess = dataAccess;
    }

    /**
     * Clear the application data (users, games, authentication)
     * @return the clear web result
     */
    public ClearResult clearApplication() {
        try {
            dataAccess.clearData();
            return new ClearResult(null, true);
        } catch (DataAccessException exception) {
            return new ClearResult("Error: " + exception.getMessage(), false);
        }
    }
}
