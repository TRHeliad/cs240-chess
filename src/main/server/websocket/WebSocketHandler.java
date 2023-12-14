package server.websocket;

import chess.ChessGame;
import chess.ChessGameImpl;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.Game;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;


@WebSocket
public class WebSocketHandler {
    private final DataAccess dataAccess;
    private static final Gson gameAdapter = ChessGameImpl.getGsonAdapter();
    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userCommand = gameAdapter.fromJson(message, UserGameCommand.class);
        try {
            switch (userCommand.getCommandType()) {
                case JOIN_PLAYER -> joinPlayer(userCommand.getAuthString(), userCommand.getGameID(), false,
                        userCommand.getPlayerColor(), session);
                case JOIN_OBSERVER -> joinPlayer(userCommand.getAuthString(), userCommand.getGameID(), true,
                        null, session);
                case MAKE_MOVE -> makeMove(userCommand.getAuthString(), userCommand.getGameID(),
                        userCommand.getChessMove(), session);
                case LEAVE -> leave(userCommand.getAuthString(), userCommand.getGameID(), false, session);
                case RESIGN -> leave(userCommand.getAuthString(), userCommand.getGameID(), true, session);
            }
        } catch(Exception exception) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage(exception.getMessage());
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        }
    }

    public void clear() {
        connections.clear();
    }

    private void joinPlayer(String authToken, Integer gameID, boolean isObserver, ChessGame.TeamColor playerColor,
                            Session session) throws Exception {
        connections.add(gameID, session);
        var username = dataAccess.getAuthToken(authToken).username();
        var gameEntry = dataAccess.getGame(gameID);

        if (!isObserver && playerColor == null)
        {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("No team color given");
            session.getRemote().sendString(gameAdapter.toJson(errorMessage));
        } else {
            String joinMessage;
            if (playerColor == null)
                joinMessage = String.format("%s joined the game as an observer", username);
            else
                joinMessage = String.format("%s joined the game as " + playerColor.toString(), username);

            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            serverMessage.setMessage(joinMessage);
            connections.broadcastGameMessage(gameEntry, serverMessage, session);

            ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadMessage.setGame(gameEntry.game());
            session.getRemote().sendString(gameAdapter.toJson(loadMessage));
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws Exception {
        var username = dataAccess.getAuthToken(authToken).username();
        var gameEntry = dataAccess.getGame(gameID);

        var whiteUsername = gameEntry.whiteUsername();
        var blackUsername = gameEntry.blackUsername();
        var isBlack = blackUsername != null && blackUsername.equals(username);
        var isWhite = whiteUsername != null && whiteUsername.equals(username);

        if (gameEntry.gameOver()) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            errorMessage.setErrorMessage("Game is over");
            session.getRemote().sendString(gameAdapter.toJson(errorMessage));
        } else {
            if (!isBlack && !isWhite)
                throw new Exception(String.format("User %s isn't a player", username));

            var chessGame = gameEntry.game();
            var piece = chessGame.getBoard().getPiece(move.getStartPosition());
            if (piece.getTeamColor() == BLACK && isWhite ||
                    piece.getTeamColor() == WHITE && isBlack)
                throw new InvalidMoveException("That's not your piece!");

            chessGame.makeMove(move);
            var newGame = new Game(gameID, gameEntry.whiteUsername(), gameEntry.blackUsername(),
                    gameEntry.gameName(), chessGame, gameEntry.gameOver());
            dataAccess.updateGame(newGame);

            ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadMessage.setGame(gameEntry.game());
            connections.broadcastGameMessage(newGame, loadMessage, null);

            ServerMessage moveMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            moveMessage.setMessage(String.format("%s made move %s to %s", username, move.getStartPosition().toString(),
                    move.getEndPosition().toString()));
            connections.broadcastGameMessage(newGame, moveMessage, session);

            ChessGame.TeamColor winnerColor = null;
            ChessGame.TeamColor checkColor = null;
            String checkName = null;
            String winnerName = null;
            boolean gameOver = false;

            if (chessGame.isInCheckmate(WHITE)) {
                winnerColor = WHITE;
                winnerName = whiteUsername;
                gameOver = true;
            } else if (chessGame.isInCheckmate(BLACK)) {
                winnerColor = BLACK;
                winnerName = blackUsername;
                gameOver = true;
            } else if (chessGame.isInStalemate(WHITE) && chessGame.getTeamTurn() == WHITE ||
                    chessGame.isInStalemate(BLACK) && chessGame.getTeamTurn() == BLACK)
                completeGame(gameID, null, false);
            else if (chessGame.isInCheck(WHITE)) {
                checkColor = WHITE;
                checkName = whiteUsername;
            } else if (chessGame.isInCheck(BLACK)) {
                checkColor = BLACK;
                checkName = blackUsername;
            }

            if (winnerColor != null) {
                ServerMessage checkmateMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                if (winnerName != null)
                    checkmateMessage.setMessage(String.format("%s (%s) checkmated", winnerName, winnerColor));
                else
                    checkmateMessage.setMessage(String.format("%s checkmated", winnerColor));
                connections.broadcastGameMessage(newGame, checkmateMessage, null);
            }

            if (checkColor != null) {
                ServerMessage checkMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                if (checkName != null)
                    checkMessage.setMessage(String.format("%s (%s) is in check", checkName, checkColor));
                else
                    checkMessage.setMessage(String.format("%s is in check", checkColor));
                connections.broadcastGameMessage(newGame, checkMessage, null);
            }

            if (gameOver)
                completeGame(gameID, winnerColor, false);
        }
    }

    private void leave(String authToken, Integer gameID, boolean resigned, Session session) throws Exception {
        var username = dataAccess.getAuthToken(authToken).username();
        var gameEntry = dataAccess.getGame(gameID);

        var whiteUsername = gameEntry.whiteUsername();
        var blackUsername = gameEntry.blackUsername();

        if (resigned) {
            ChessGame.TeamColor winnerColor = null;
            if (username.equals(whiteUsername))
                winnerColor = BLACK;
            else if (username.equals(blackUsername))
                winnerColor = WHITE;

            if (winnerColor == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Observers can't resign");
                session.getRemote().sendString(new Gson().toJson(errorMessage));
            } else {
                ServerMessage leaveMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                leaveMessage.setMessage(String.format("%s resigned from the game", username));
                connections.broadcastGameMessage(gameEntry, leaveMessage, null);

                completeGame(gameID, winnerColor, true);
            }
        } else {
            ServerMessage leaveMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            leaveMessage.setMessage(String.format("%s left the game", username));
            connections.broadcastGameMessage(gameEntry, leaveMessage, session);
        }

        if (username.equals(whiteUsername))
            whiteUsername = null;
        if (username.equals(blackUsername))
            blackUsername = null;

        var newGame = new Game(gameID, whiteUsername, blackUsername, gameEntry.gameName(), gameEntry.game(),
                gameEntry.gameOver());
        dataAccess.updateGame(newGame);

        connections.remove(gameID, session);
    }

    private void completeGame(Integer gameID, ChessGame.TeamColor winnerColor, boolean resigned) throws Exception {
        var gameEntry = dataAccess.getGame(gameID);
        String winningUser = null;
        if (winnerColor == WHITE)
            winningUser = gameEntry.whiteUsername();
        if (winnerColor == BLACK)
            winningUser = gameEntry.blackUsername();

        if (!resigned) {
            ServerMessage resultMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            if (winnerColor != null)
                resultMessage.setMessage(String.format("%s (%s) won the game!", winningUser, winnerColor.toString()));
            else
                resultMessage.setMessage("The game ended in a stalemate!");
            connections.broadcastGameMessage(gameEntry, resultMessage, null);
        }

        var chessGame = gameEntry.game();
//        chessGame.getBoard().resetBoard();
//        chessGame.setTeamTurn(WHITE);
        var newGame = new Game(gameID, gameEntry.whiteUsername(), gameEntry.blackUsername(), gameEntry.gameName(),
                chessGame, false);
        dataAccess.updateGame(newGame);
    }
}