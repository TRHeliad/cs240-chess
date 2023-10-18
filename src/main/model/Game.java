package model;

import chess.ChessGame;

/**
 * Contains data for a chess game
 * @param gameID The ID of the game
 * @param whiteUsername Username of the white player
 * @param blackUsername Username of the black player
 * @param gameName Name of the game
 * @param game The `ChessGame` object
 */
public record Game(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) { }
