package unitTests.clientTests;

import serverFacade.ServerFacade;
import webRequest.CreateGameRequest;

public class TestFactory {

    public static ServerFacade getServerFacade() {
        return new ServerFacade("localhost", "8080");
    }

    public static CreateGameRequest getSimpleCreateGameRequest() {
        return new CreateGameRequest("TestGame");
    }
}
