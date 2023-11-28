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
        serverFacade.clear();
        var tokenRequest = new RegisterRequest("auth", "auth", "auth");
        var tokenResult = serverFacade.register(tokenRequest);
        testAuthToken = tokenResult.authToken();
    }

    @Test
    public void clear() {
        serverFacade.clear();
        var createGameRequest = new CreateGameRequest("name");
        serverFacade.createGame(createGameRequest, testAuthToken);
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void registerUser() {
        var registerRequest = new RegisterRequest("test", "user", "email");
        var registerResult = serverFacade.register(registerRequest);
        Assertions.assertEquals(registerResult.username(), "test");
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void registerDuplicateUser() {
        var registerRequest = new RegisterRequest("test", "user", "email");
        var registerResult = serverFacade.register(registerRequest);
        var registerResult2 = serverFacade.register(registerRequest);
        Assertions.assertFalse(registerResult2.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void login() {
        var loginRequest = new LoginRequest("auth", "auth");
        var loginResult = serverFacade.login(loginRequest);
        Assertions.assertTrue(loginResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void loginWrongPassword() {
        var loginRequest = new LoginRequest("auth", "wrongpassword");
        var loginResult = serverFacade.login(loginRequest);
        Assertions.assertFalse(loginResult.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void logout() {
        var logoutRequest = new LogoutRequest(testAuthToken);
        var logoutResult = serverFacade.logout(logoutRequest);
        Assertions.assertTrue(logoutResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void logoutNoAuthentication() {
        var logoutRequest = new LogoutRequest("");
        var logoutResult = serverFacade.logout(logoutRequest);
        Assertions.assertFalse(logoutResult.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void createGame() {
        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);
        Assertions.assertTrue(createGameResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void createGameNoAuthentication() {
        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, "");
        Assertions.assertFalse(createGameResult.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void listGames() {
        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);
        var listGamesResult = serverFacade.listGames(testAuthToken);
        Assertions.assertTrue(listGamesResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
        Assertions.assertEquals(listGamesResult.games().length, 1);
    }

    @Test
    public void listGamesNoAuthentication() {
        var listGamesResult = serverFacade.listGames("");
        Assertions.assertFalse(listGamesResult.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void joinGame() {
        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

        var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResult.gameID());
        var joinGameResult = serverFacade.joinGame(joinGameRequest, testAuthToken);

        Assertions.assertTrue(joinGameResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void joinGameObserver() {
        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

        var joinGameRequest = new JoinGameRequest(null, createGameResult.gameID());
        var joinGameResult = serverFacade.joinGame(joinGameRequest, testAuthToken);

        Assertions.assertTrue(joinGameResult.success());
        Assertions.assertEquals(serverFacade.getStatusCode(), 200);
    }

    @Test
    public void joinGameFullGame() {
        var registerRequest = new RegisterRequest("user", "user", "user");
        var secondUserResult = serverFacade.register(registerRequest);

        var createGameRequest = TestFactory.getSimpleCreateGameRequest();
        var createGameResult = serverFacade.createGame(createGameRequest, testAuthToken);

        var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.BLACK, createGameResult.gameID());
        serverFacade.joinGame(joinGameRequest, testAuthToken);
        var joinGameResult = serverFacade.joinGame(joinGameRequest, secondUserResult.authToken());

        Assertions.assertFalse(joinGameResult.success());
        Assertions.assertNotEquals(serverFacade.getStatusCode(), 200);
    }
}
