package service;

import webRequest.LoginRequest;
import webRequest.RegisterRequest;
import webResult.LoginResult;
import webResult.LogoutResult;
import webResult.RegisterResult;


/**
 * The web service for user requests
 */
public class UserService {

    /**
     * Login a user (get authentication for)
     * @param request The login data
     * @return the login result
     */
    LoginResult login(LoginRequest request) {
        return null;
    }

    /**
     * Remove authentication for the current session
     * @return the logout result
     */
    LogoutResult logout() {
        return null;
    }

    /**
     * Register a new user
     * @param request The registration data
     * @return the registration result
     */
    RegisterResult register(RegisterRequest request) {
        return null;
    }
}
