package webRequest;

/**
 * Contains request data for logging in
 * @param authToken The token to de-authorize
 */
public record LogoutRequest(String authToken) { }