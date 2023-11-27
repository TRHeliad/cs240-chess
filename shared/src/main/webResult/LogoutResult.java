package webResult;

/**
 * The result data for a logout request
 * @param message A potential error message
 */
public record LogoutResult(String message, Boolean success) { }
