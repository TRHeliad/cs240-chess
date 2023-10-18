package model;

/**
 * Contains authentication data
 * @param authToken The token for authentication
 * @param username The authenticated username
 */
public record AuthToken(String authToken, String username) { }
