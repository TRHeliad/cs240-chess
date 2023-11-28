package webResult;

import model.Game;

/**
 * Contains the result data of a join game request
 * @param message A potential error message
 */
public record JoinGameResult(String message, Boolean success, Game game) { }
