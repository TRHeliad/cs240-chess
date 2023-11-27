package webRequest;

/**
 * Contains request data for logging in
 * @param username Of the user
 * @param password For the user
 */
public record LoginRequest(String username, String password) { }
