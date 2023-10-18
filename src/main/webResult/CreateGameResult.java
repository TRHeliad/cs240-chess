package webResult;

/**
 * Contains the result data of a create game request
 * @param gameID The ID of the new game
 * @param message A potential error message
 */
public record CreateGameResult(Integer gameID, String message) { }
