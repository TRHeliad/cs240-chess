package webSocketMessages.serverMessages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    ChessGame game;
    String message;
    String errorMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }
    public String getMessage() { return this.message; }
    public String getErrorMessage() { return this.errorMessage; }
    public ChessGame getGame() { return this.game; }

    public void setServerMessageType(ServerMessageType messageType) { this.serverMessageType = messageType; }
    public void setMessage(String message) { this.message = message; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setGame(ChessGame game) { this.game = game; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServerMessage))
            return false;
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
