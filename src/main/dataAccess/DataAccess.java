package dataAccess;

import chess.ChessGame;
import model.AuthToken;
import model.Game;
import model.User;

import java.util.Collection;

/**
 * Provides access to stored chess data
 */
public interface DataAccess {
    /**
     * Adds an entry with the user in the database
     * @param user The user to add
     * @throws DataAccessException when data access fails
     */
    void createUser(User user) throws DataAccessException;

    /**
     * @return the users in the database
     * @throws DataAccessException when data access fails
     */
    Collection<User> getUsers() throws DataAccessException;

    /**
     * Remove the entry for the given user from the database
     * @param user The user to remove
     * @throws DataAccessException when data access fails
     */
    void destroyUser(User user) throws DataAccessException;

    /**
     * Adds an entry with the authToken in the database
     * @param authToken The authToken to add
     * @throws DataAccessException when data access fails
     */
    void createAuthToken(AuthToken authToken) throws DataAccessException;

    /**
     * @return the authTokens in the database
     * @throws DataAccessException when data access fails
     */
    Collection<AuthToken> getAuthTokens() throws DataAccessException;

    /**
     * Remove the entry for the given authToken from the database
     * @param authToken The authToken to remove
     * @throws DataAccessException when data access fails
     */
    void destroyAuth(AuthToken authToken) throws DataAccessException;

    /**
     * Adds an entry with the game in the database
     * @param game The game to add
     * @throws DataAccessException when data access fails
     */
    void createGame(Game game) throws DataAccessException;

    /**
     * @return the games in the database
     * @throws DataAccessException when data access fails
     */
    Collection<Game> getGames() throws DataAccessException;

    /**
     * Joins the user to the specified game
     * @param user The game to add
     * @param userColor The team color of the user
     * @param game The game to add
     * @throws DataAccessException when data access fails
     */
    void joinGame(User user, ChessGame.TeamColor userColor, Game game) throws DataAccessException;

    /**
     * Remove the entry for the given game from the database
     * @param game The game to remove
     * @throws DataAccessException when data access fails
     */
    void destroyGame(Game game) throws DataAccessException;

    /**
     * Clears all user, authentication, and game data from the database
     * @throws DataAccessException when data access fails
     */
    void clearData() throws DataAccessException;
}
