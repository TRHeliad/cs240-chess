package service;

import webRequest.CreateGameRequest;
import webRequest.JoinGameRequest;
import webResult.CreateGameResult;
import webResult.JoinGameResult;
import webResult.ListGamesResult;

/**
 * The web service for game requests
 */
public class GameService {
    /**
     * Get the saved games
     * @return the list games result
     */
    ListGamesResult listGames() {
        return null;
    }

    /**
     * Create a new game
     * @param request The create game request data
     * @return the game creation result
     */
    CreateGameResult createGame(CreateGameRequest request) {
        return null;
    }

    /**
     * Join a user to a game
     * @param request The join game request data
     * @return the join game result
     */
    JoinGameResult joinGame(JoinGameRequest request) {
        return null;
    }
}
