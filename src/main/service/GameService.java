package service;

import chess.ChessGame;
import chess.ChessGameImpl;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.Game;
import webRequest.CreateGameRequest;
import webRequest.JoinGameRequest;
import webResult.CreateGameResult;
import webResult.JoinGameResult;
import webResult.ListGamesResult;

/**
 * The web service for game requests
 */
public class GameService {
    private static final GameService gameService = new GameService();
    public static GameService getInstance() {
        return gameService;
    }
    private DataAccess dataAccess;
    public void init(DataAccess dataAccess) {
        gameService.dataAccess = dataAccess;
    }

    /**
     * Get the saved games
     * @param authToken Token for authorization
     * @return the list games result
     */
    public ListGamesResult listGames(String authToken) {
        try {
            var authTokenObject = dataAccess.getAuthToken(authToken);
            if (authTokenObject != null) {
                return new ListGamesResult(dataAccess.getGames().toArray(new Game[0]), null, true);
            } else {
                return new ListGamesResult(null, "Error: unauthorized", false);
            }
        } catch (DataAccessException exception) {
            return new ListGamesResult(null, "Error: " + exception.getMessage(), false);
        }
    }

    /**
     * Create a new game
     * @param request The create game request data
     * @param authToken Token for authorization
     * @return the game creation result
     */
    public CreateGameResult createGame(CreateGameRequest request, String authToken) {
        try {
            var authTokenObject = dataAccess.getAuthToken(authToken);
            if (authTokenObject != null) {
                var chessGame = new ChessGameImpl();
                var game = new Game(0, null, null, request.gameName(), chessGame);
                var gameID = dataAccess.createGame(game);
                return new CreateGameResult(gameID, null, true);
            } else {
                return new CreateGameResult(null, "Error: unauthorized", false);
            }
        } catch (DataAccessException exception) {
            return new CreateGameResult(null, "Error: " + exception.getMessage(), false);
        }
    }

    /**
     * Join a user to a game
     * @param request The join game request data
     * @param authToken Token for authorization
     * @return the join game result
     */
    public JoinGameResult joinGame(JoinGameRequest request, String authToken) {
        try {
            if (request.gameID() == null)
                return new JoinGameResult("Error: bad request", false);

            var authTokenObject = dataAccess.getAuthToken(authToken);
            if (authTokenObject != null) { // valid token
                var game = dataAccess.getGame(request.gameID());
                if (game == null)
                    return new JoinGameResult("Error: bad request", false);
                else {
                    String currentPlayerOnTeam;
                    var isObserver = request.playerColor() == null;
                    if (isObserver) {
                        return new JoinGameResult(null, true);
                    } else {
                        var isWhite = request.playerColor() == ChessGame.TeamColor.WHITE;
                        if (isWhite)
                            currentPlayerOnTeam = game.whiteUsername();
                        else
                            currentPlayerOnTeam = game.blackUsername();
                        if (currentPlayerOnTeam == null) { // team slot in game not taken
                            var username = dataAccess.getAuthToken(authToken).username();
                            dataAccess.updateGame(new Game(
                                    game.gameID(),
                                    isWhite ? username : game.whiteUsername(),
                                    !isWhite ? username : game.blackUsername(),
                                    game.gameName(),
                                    game.game()
                            ));
                            return new JoinGameResult(null, true);
                        } else
                            return new JoinGameResult("Error: already taken", false);
                    }
                }
            } else {
                return new JoinGameResult("Error: unauthorized", false);
            }
        } catch (DataAccessException exception) {
            return new JoinGameResult("Error: " + exception.getMessage(), false);
        }
    }
}
