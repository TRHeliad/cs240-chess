package dataAccess;

import chess.ChessGame;
import model.AuthToken;
import model.Game;
import model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, User> users = new HashMap<>();
    private HashMap<String, AuthToken> authTokens = new HashMap<>();
    private HashMap<Integer, Game> games = new HashMap<>();

    private static MemoryDataAccess memoryDataAccess = new MemoryDataAccess();

    public static MemoryDataAccess getInstance() {
        return memoryDataAccess;
    }

    @Override
    public void createUser(User user) throws DataAccessException {
        if (users.containsKey(user.username()))
            throw new DataAccessException("already taken");
        users.put(user.username(), user);
    }

    @Override
    public User getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void destroyUser(User user) throws DataAccessException {
        users.remove(user.username());
    }

    @Override
    public void createAuthToken(AuthToken authToken) throws DataAccessException {
        if (users.containsKey(authToken.authToken()))
            throw new DataAccessException("description");
        authTokens.put(authToken.authToken(), authToken);
    }

    @Override
    public AuthToken getAuthToken(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void destroyAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    @Override
    public void createGame(Game game) throws DataAccessException {
        if (games.containsKey(game.gameID()))
            throw new DataAccessException("gameID already taken");
        games.put(game.gameID(), game);
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        if (!games.containsKey(game.gameID()))
            throw new DataAccessException("game does not exist");
        games.put(game.gameID(), game);
    }

    @Override
    public Collection<Game> getGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public Game getGame(Integer gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public void joinGame(User user, ChessGame.TeamColor userColor, Game game) throws DataAccessException {
        var gameID = game.gameID();
        if (!games.containsKey(gameID))
            throw new DataAccessException("bad request");
        var oldGame = games.get(gameID);
        games.put(gameID, new Game(
                gameID,
                userColor == ChessGame.TeamColor.WHITE ? user.username() : oldGame.whiteUsername(),
                userColor == ChessGame.TeamColor.BLACK ? user.username() : oldGame.blackUsername(),
                oldGame.gameName(),
                oldGame.game()
        ));
    }

    @Override
    public void destroyGame(Game game) throws DataAccessException {
        games.remove(game.gameID());
    }

    @Override
    public void clearData() throws DataAccessException {
        users = new HashMap<>();
        authTokens = new HashMap<>();
        games = new HashMap<>();
    }
}