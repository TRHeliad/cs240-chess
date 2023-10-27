package webResult;

/**
 * The result data for a user registration request
 * @param username The user's username
 * @param authToken A new authentication token
 * @param message A potential error message
 */
public record RegisterResult(String username, String authToken, String message, Boolean success) { }
