package webSocketMessages.userCommands;

import chess.ChessGame;
import chess.ChessMove;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    public UserGameCommand(String authToken) {
        this.authToken = authToken;
    }

    public enum CommandType {
        JOIN_PLAYER,
        JOIN_OBSERVER,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    protected CommandType commandType;

    private final String authToken;
    private ChessMove chessMove;
    private Integer gameID;

    private ChessGame.TeamColor playerColor;

    public String getAuthString() {
        return authToken;
    }

    public CommandType getCommandType() {
        return this.commandType;
    }

    public ChessMove getChessMove() { return this.chessMove; }
    public Integer getGameID() { return this.gameID; }
    public ChessGame.TeamColor getPlayerColor() { return this.playerColor; }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public void setChessMove(ChessMove chessMove) { this.chessMove = chessMove; }
    public void setGameID(Integer gameID) { this.gameID = gameID; }
    public void setPlayerColor(ChessGame.TeamColor playerColor) { this.playerColor = playerColor; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserGameCommand))
            return false;
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() && Objects.equals(getAuthString(), that.getAuthString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthString());
    }
}
