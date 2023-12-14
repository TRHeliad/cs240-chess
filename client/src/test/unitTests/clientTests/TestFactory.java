package unitTests.clientTests;

import serverFacade.ServerFacade;
import serverFacade.ServerMessageObserver;
import webRequest.CreateGameRequest;
import webSocketMessages.serverMessages.ServerMessage;

public class TestFactory {

    public static ServerFacade getServerFacade() {
        ServerMessageObserver serverMessageObserver = new ServerMessageObserver() {
            public void notify(ServerMessage message) {}
        };
        return new ServerFacade("localhost", "8080", serverMessageObserver);
    }

    public static CreateGameRequest getSimpleCreateGameRequest() {
        return new CreateGameRequest("TestGame");
    }
}
