package webRequest;

/**
 * Contains the request data for registering a user
 * @param username New user's username
 * @param password New user's password
 * @param email New user's email
 */
public record RegisterRequest(String username, String password, String email) { }
