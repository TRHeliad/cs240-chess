package passoffTests.serverTests;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthToken;
import model.Game;
import model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import passoffTests.TestFactory;
import spark.utils.Assert;

public class DatabaseTests {
    private static final DataAccess dataAccess = TestFactory.getDataAccess();
    @BeforeEach
    public void setup() {
        try {
            dataAccess.clearData();
        } catch (DataAccessException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Test
    public void clearDatabase() {
        try {
            Game game = TestFactory.createSimpleGame();
            dataAccess.createGame(game);
            dataAccess.clearData();
            var resultGame = dataAccess.getGame(game.gameID());
            Assertions.fail("Retrieved game that should have been deleted");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "bad request");
        }
    }

    @Test
    public void createAndGetUser() {
        try {
            User user = TestFactory.createSimpleUser();
            dataAccess.createUser(user);
            var resultUser = dataAccess.getUser(user.username());
            Assertions.assertNotNull(resultUser);
            Assertions.assertEquals(resultUser, user);
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void createDuplicateUser() {
        try {
            User user = TestFactory.createSimpleUser();
            dataAccess.createUser(user);
            dataAccess.createUser(user);
            Assertions.fail("Creating duplicate didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "already taken");
        }
    }

    @Test
    public void getNonexistentUser() {
        try {
            User user = TestFactory.createSimpleUser();
            var resultUser = dataAccess.getUser(user.username());
            Assertions.assertNull(resultUser, "Returned a user that shouldn't exist");
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void destroyUser() {
        try {
            User user = TestFactory.createSimpleUser();
            dataAccess.createUser(user);
            dataAccess.destroyUser(user);
            var resultUser = dataAccess.getUser(user.username());
            Assertions.assertNull(resultUser, "Returned a user that shouldn't exist");
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void destroyUserWithoutUsername() {
        try {
            User user = TestFactory.createSimpleUser();
            User badUser = new User(null, user.password(), user.email());
            dataAccess.createUser(user);
            dataAccess.destroyUser(badUser);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "Username was null");
        }
    }

    @Test
    public void createAndGetAuthToken() {
        try {
            var authToken = TestFactory.createSimpleAuthToken();
            dataAccess.createAuthToken(authToken);
            var resultToken = dataAccess.getAuthToken(authToken.authToken());
            Assertions.assertNotNull(resultToken);
            Assertions.assertEquals(resultToken, authToken);
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void createAuthTokenWithoutUsername() {
        try {
            var authToken = new AuthToken("testToken", null);
            dataAccess.createAuthToken(authToken);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "Username was null");
        }
    }

    @Test
    public void getNonexistentAuthToken() {
        try {
            var resultToken = dataAccess.getAuthToken("testToken");
            Assertions.assertNull(resultToken, "A nonexistent token was returned");
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void destroyAuth() {
        try {
            var authToken = TestFactory.createSimpleAuthToken();
            dataAccess.createAuthToken(authToken);
            dataAccess.destroyAuth(authToken.authToken());
            var resultToken = dataAccess.getAuthToken(authToken.authToken());
            Assertions.assertNull(resultToken, "The token was not destroyed");
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void destroyAuthWithoutToken() {
        try {
            var authToken = TestFactory.createSimpleAuthToken();
            dataAccess.createAuthToken(authToken);
            dataAccess.destroyAuth(null);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "authToken was null");
        }
    }

    @Test
    public void createAndGetGame() {
        try {
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            var resultGame = dataAccess.getGame(gameID);
            Assertions.assertNotNull(resultGame);
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void createGameWithoutName() {
        try {
            Game game = new Game(0, null, null, null, TestFactory.getNewGame());
            dataAccess.createGame(game);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "gameName was null");
        }
    }

    @Test
    public void updateGame() {
        try {
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            
            Game updatedGame = new Game(
                    gameID,
                    "newUsername",
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame);
            var resultGame = dataAccess.getGame(gameID);
            Assertions.assertNotNull(resultGame);
            Assertions.assertEquals(resultGame.whiteUsername(), "newUsername");
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void updateNonexistentGame() {
        try {
            Game game = TestFactory.createSimpleGame();
            dataAccess.updateGame(game);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "bad request");
        }
    }

    @Test
    public void getGames() {
        try {
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            var resultGames = dataAccess.getGames();
            Assertions.assertNotNull(resultGames);
            Assertions.assertFalse(resultGames.isEmpty());
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }
    
    @Test
    public void getNonexistentGame() {
        try {
            var resultGame = dataAccess.getGame(123);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "bad request");
        }
    }

    @Test
    public void joinGame() {
        try {
            User user = TestFactory.createSimpleUser();
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            dataAccess.createUser(user);
            var idGame = new Game(gameID, null, null, game.gameName(), game.game());
            dataAccess.joinGame(user, ChessGame.TeamColor.BLACK, idGame);
            var gameResult = dataAccess.getGame(gameID);
            Assertions.assertEquals(gameResult.blackUsername(), user.username());
        } catch (DataAccessException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void joinNonexistentGame() {
        try {
            User user = TestFactory.createSimpleUser();
            Game game = TestFactory.createSimpleGame();
            dataAccess.createUser(user);
            dataAccess.joinGame(user, ChessGame.TeamColor.BLACK, game);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "bad request");
        }
    }

    @Test
    public void destroyGame() {
        try {
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            dataAccess.destroyGame(gameID);
            dataAccess.getGame(gameID);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "bad request");
        }
    }

    @Test
    public void destroyGameWithoutID() {
        try {
            Game game = TestFactory.createSimpleGame();
            var gameID = dataAccess.createGame(game);
            dataAccess.destroyGame(null);
            Assertions.fail("Didn't throw error");
        } catch (DataAccessException exception) {
            Assertions.assertEquals(exception.getMessage(), "gameID was null");
        }
    }

}
