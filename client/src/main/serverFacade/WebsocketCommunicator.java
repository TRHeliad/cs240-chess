package serverFacade;

import chess.ChessGameImpl;
import com.google.gson.Gson;
import serverFacade.ServerMessageObserver;
import webSocketMessages.serverMessages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebsocketCommunicator extends Endpoint {
    private static final Gson gameAdapter = ChessGameImpl.getGsonAdapter();
    public Session session;
    public WebsocketCommunicator(String address, String port, ServerMessageObserver messageObserver) throws Exception {
        URI uri = new URI(String.format("ws://%s:%s/connect", address, port));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage serverMessage = gameAdapter.fromJson(message, ServerMessage.class);
                messageObserver.notify(serverMessage);
            }
        });
    }

    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }

    public void close() throws IOException {
        this.session.close();
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
