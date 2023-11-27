package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthToken;
import model.User;
import webRequest.LoginRequest;
import webRequest.RegisterRequest;
import webResult.LoginResult;
import webResult.LogoutResult;
import webResult.RegisterResult;

import java.util.UUID;


/**
 * The web service for user requests
 */
public class UserService {
    private static final UserService userService = new UserService();

    public static UserService getInstance() {
        return userService;
    }

    private DataAccess dataAccess;

    public void init(DataAccess dataAccess) {
        userService.dataAccess = dataAccess;
    }

    /**
     * Login a user (get authentication for)
     * @param request The login data
     * @return the login result
     */
    public LoginResult login(LoginRequest request) {
        try {
            var user = dataAccess.getUser(request.username());
            if (user != null && request.password().equals(user.password())) {
                var authToken = new AuthToken(UUID.randomUUID().toString(), request.username());
                dataAccess.createAuthToken(authToken);
                return new LoginResult(request.username(), authToken.authToken(), null, true);
            } else
                return new LoginResult(null, null, "Error: unauthorized", false);
        } catch (DataAccessException exception) {
            return new LoginResult(null, null,
                    "Error: " + exception.getMessage(), false);
        }
    }

    /**
     * Remove authentication for the current session
     * @return the logout result
     */
    public LogoutResult logout(String authToken) {
        try {
            var authTokenObject = dataAccess.getAuthToken(authToken);
            if (authTokenObject != null) {
                dataAccess.destroyAuth(authToken);
                return new LogoutResult(null, true);
            } else {
                return new LogoutResult("Error: unauthorized", false);
            }
        } catch (DataAccessException exception) {
            return new LogoutResult("Error: " + exception.getMessage(), false);
        }
    }

    /**
     * Register a new user
     * @param request The registration data
     * @return the registration result
     */
    public RegisterResult register(RegisterRequest request) {
        try {
            if (request.username() == null || request.username().equals("") ||
                    request.password() == null || request.password().equals(""))
                return new RegisterResult(null, null,
                        "Error: bad request", false);

            dataAccess.createUser(new User(
                    request.username(),
                    request.password(),
                    request.email()
            ));

            var loginRequest = new LoginRequest(request.username(), request.password());
            var authToken = login(loginRequest).authToken();
            return new RegisterResult(
                    request.username(),
                    authToken,
                    null,
                    true
            );
        } catch (DataAccessException exception) {
            return new RegisterResult(null, null,
                    "Error: " + exception.getMessage(), false);
        }
    }
}
