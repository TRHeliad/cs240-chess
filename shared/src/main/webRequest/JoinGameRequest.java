package webRequest;

import chess.ChessGame;

/**
 * Contains request data for joining a game
 * @param playerColor The player's team color
 * @param gameID The ID of the game to join
 */
public record JoinGameRequest(ChessGame.TeamColor playerColor, Integer gameID) { }
