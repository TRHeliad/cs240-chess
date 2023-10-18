package webResult;

import model.Game;

/**
 * Contains the result data for a list games request
 * @param games The list of games
 * @param message A potential error message
 */
public record ListGamesResult(Game[] games, String message) {}
