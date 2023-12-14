package unitTests.clientTests;

import chess.ChessGame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverFacade.ServerFacade;
import webRequest.*;

public class ServerFacadeTests {
    private static final ServerFacade serverFacade = TestFactory.getServerFacade();
    private static String testAuthToken;

    @BeforeEach
    public void setup() {
        try {
            serverFacade.clear();
            var tokenRequest = new RegisterRequest("auth", "auth", "auth");
            var tokenResult = serverFacade.register(tokenRequest);
            testAuthToken = tokenResult.authToken();
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    public void clear() {
        try {
            serverFacade.clear();
            var createGameRequest = new CreateGameRequest("name");
            serverFacade.createGame(createGameRequest, testAuthToken);
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void registerUser() {
        try {
            var registerRequest = new RegisterRequest("test", "user", "email");
            var registerResult = serverFacade.register(registerRequest);
            Assertions.assertEquals(registerResult.username(), "test");
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void registerDuplicateUser() {
        try {
            var registerRequest = new RegisterRequest("test", "user", "email");
            var registerResult = serverFacade.register(registerRequest);
            var registerResult2 = serverFacade.register(registerRequest);
            Assertions.assertFalse(registerResult2.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void login() {
        try {
            var loginRequest = new LoginRequest("auth", "auth");
            var loginResult = serverFacade.login(loginRequest);
            Assertions.assertTrue(loginResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void loginWrongPassword() {
        try {
            var loginRequest = new LoginRequest("auth", "wrongpassword");
            var loginResult = serverFacade.login(loginRequest);
            Assertions.assertFalse(loginResult.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void logout() {
        try {
            var logoutRequest = new LogoutRequest(testAuthToken);
            var logoutResult = serverFacade.logout(logoutRequest);
            Assertions.assertTrue(logoutResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void logoutNoAuthentication() {
        try {
            var logoutRequest = new LogoutRequest("");
            var logoutResult = serverFacade.logout(logoutRequest);
            Assertions.assertFalse(logoutResult.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void createGame() {
        try {
            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);
            Assertions.assertTrue(createGameResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void createGameNoAuthentication() {
        try {
            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, "");
            Assertions.assertFalse(createGameResult.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void listGames() {
        try {
            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);
            var listGamesResult = serverFacade.listGames(testAuthToken);
            Assertions.assertTrue(listGamesResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
            Assertions.assertEquals(listGamesResult.games().length, 1);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void listGamesNoAuthentication() {
        try {
            var listGamesResult = serverFacade.listGames("");
            Assertions.assertFalse(listGamesResult.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void joinGame() {
        try {
            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

            var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResult.gameID());
            var joinGameResult = serverFacade.joinGame(joinGameRequest, testAuthToken);

            Assertions.assertTrue(joinGameResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void joinGameObserver() {
        try {
            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

            var joinGameRequest = new JoinGameRequest(null, createGameResult.gameID());
            var joinGameResult = serverFacade.joinGame(joinGameRequest, testAuthToken);

            Assertions.assertTrue(joinGameResult.success());
            Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    public void joinGameFullGame() {
        try {
            var registerRequest = new RegisterRequest("user", "user", "user");
            var secondUserResult = serverFacade.register(registerRequest);

            var createGameRequest = TestFactory.getSimpleCreateGameRequest();
            var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

            var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResult.gameID());
            serverFacade.joinGame(joinGameRequest, testAuthToken);
            var joinGameResult = serverFacade.joinGame(joinGameRequest, secondUserResult.authToken());

            Assertions.assertFalse(joinGameResult.success());
            Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
        } catch(Exception exception) {
            Assertions.fail(exception.getMessage());
        }
    }
}
