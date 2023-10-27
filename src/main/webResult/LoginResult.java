package webResult;

/**
 * Contains the result data for a login request
 * @param username The user's username
 * @param authToken The new authentication token
 * @param message A potential error message
 */
public record LoginResult(String username, String authToken, String message, Boolean success) { }
