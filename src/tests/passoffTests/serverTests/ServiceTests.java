package passoffTests.serverTests;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.User;
import org.junit.jupiter.api.*;
import passoffTests.TestFactory;
import service.ApplicationService;
import service.GameService;
import service.UserService;
import webRequest.LoginRequest;
import webRequest.LogoutRequest;
import webRequest.RegisterRequest;

public class ServiceTests {
    private static final UserService userService = TestFactory.getUserService();
    private static final GameService gameService = TestFactory.getGameService();
    private static final ApplicationService applicationService = TestFactory.getApplicationService();
    private static final DataAccess dataAccess = TestFactory.getDataAccess();

    @BeforeAll
    public static void init() {
        userService.init(dataAccess);
        gameService.init(dataAccess);
        applicationService.init(dataAccess);
    }
    @BeforeEach
    public void setup() { applicationService.clearApplication(); }

    @Test
    public void clearData() {
        var user = TestFactory.createSimpleUser();
        userService.register(new RegisterRequest(user.username(), user.password(), user.email()));
        applicationService.clearApplication();
        try {
            Assertions.assertNull(dataAccess.getUser(user.username()));
        } catch (DataAccessException exception) {
            Assertions.fail("Exception thrown while checking if user exists");
        }
    }

    @Test
    public void registerUser() {
        var user = TestFactory.createSimpleUser();
        var result = userService.register(new RegisterRequest(user.username(), user.password(), user.email()));
        Assertions.assertTrue(result.success());

        try {
            Assertions.assertNotNull(dataAccess.getUser(user.username()));
        } catch (DataAccessException exception) {
            Assertions.fail("Exception thrown while reading users");
        }
    }

    @Test
    public void doubleRegisterUser() {
        var user = TestFactory.createSimpleUser();
        userService.register(new RegisterRequest(user.username(), user.password(), user.email()));
        var result = userService.register(new RegisterRequest(user.username(), user.password(), user.email()));
        Assertions.assertFalse(result.success());
        Assertions.assertEquals(result.message(), "Error: already taken");
    }

    @Test
    public void loginUser() {
        var user = TestFactory.createSimpleUser();
        userService.register(new RegisterRequest(user.username(), user.password(), user.email()));

        var result = userService.login(new LoginRequest(user.username(), user.password()));

        Assertions.assertTrue(result.success());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void incorrectPasswordLogin() {
        var user = TestFactory.createSimpleUser();
        userService.register(new RegisterRequest(user.username(), user.password(), user.email()));

        var loginRequest = new LoginRequest(user.username(), "wrongpassword");
        var result = userService.login(loginRequest);

        Assertions.assertFalse(result.success());
        Assertions.assertNull(result.authToken());
        Assertions.assertEquals(result.message(), "Error: unauthorized");
    }

    @Test
    public void logoutUser() {
        var user = TestFactory.createSimpleUser();
        userService.register(new RegisterRequest(user.username(), user.password(), user.email()));

        var loginResult = userService.login(new LoginRequest(user.username(), user.password()));
        var authToken = loginResult.authToken();

        var result = userService.logout(authToken);

        Assertions.assertTrue(result.success());
        Assertions.assertNull(result.message());
    }
}