package serverFacade;

import chess.ChessGame;
import chess.ChessGameImpl;
import chess.ChessMove;
import com.google.gson.Gson;
import model.User;
import webRequest.*;
import webResult.*;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class ServerFacade {
    private final String address;
    private final String port;
    private static final Gson gameAdapter = ChessGameImpl.getGsonAdapter();
    private final HttpCommunicator httpCommunicator;
    private WebsocketCommunicator websocketCommunicator;
    private final ServerMessageObserver serverMessageObserver;

    public ServerFacade(String address, String port, ServerMessageObserver serverMessageObserver) {
        this.address = address;
        this.port = port;
        this.serverMessageObserver = serverMessageObserver;
        httpCommunicator = new HttpCommunicator(address, port);
    }

    public int getStatusCode() { return httpCommunicator.getStatusCode(); }

    public ClearResult clear() throws Exception { return httpCommunicator.clear(); }

    public RegisterResult register(RegisterRequest request) throws Exception { return httpCommunicator.register(request); }

    public LoginResult login(LoginRequest request) throws Exception { return httpCommunicator.login(request); }

    public LogoutResult logout(LogoutRequest request) throws Exception { return httpCommunicator.logout(request); }

    public ListGamesResult listGames(String authToken) throws Exception { return httpCommunicator.listGames(authToken); }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws Exception {
        return httpCommunicator.createGame(request, authToken);
    }
    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws Exception {
        JoinGameResult joinResult = httpCommunicator.joinGame(request, authToken);
        if (getStatusCode() == 200) {
            websocketCommunicator = new WebsocketCommunicator(address, port, serverMessageObserver);
            UserGameCommand.CommandType joinType = request.playerColor() == null ? UserGameCommand.CommandType.JOIN_OBSERVER
                    : UserGameCommand.CommandType.JOIN_PLAYER;
            var command = new UserGameCommand(authToken);
            command.setCommandType(joinType);
            command.setGameID(request.gameID());
            command.setPlayerColor(request.playerColor());
            websocketCommunicator.send(new Gson().toJson(command, UserGameCommand.class));
        }
        return joinResult;
    }

    public void makeMove(ChessMove move, Integer gameID, String authToken) throws Exception {
        verifyWebsocketExists();
        var command = new UserGameCommand(authToken);
        command.setCommandType(UserGameCommand.CommandType.MAKE_MOVE);
        command.setChessMove(move);
        command.setGameID(gameID);
        websocketCommunicator.send(gameAdapter.toJson(command, UserGameCommand.class));
    }

    public void leave(String authToken, Integer gameID) throws Exception {
        verifyWebsocketExists();
        var command = new UserGameCommand(authToken);
        command.setCommandType(UserGameCommand.CommandType.LEAVE);
        command.setGameID(gameID);
        websocketCommunicator.send(new Gson().toJson(command, UserGameCommand.class));
        websocketCommunicator.close();
    }

    public void resign(String authToken, Integer gameID) throws Exception {
        verifyWebsocketExists();
        var command = new UserGameCommand(authToken);
        command.setCommandType(UserGameCommand.CommandType.RESIGN);
        command.setGameID(gameID);
        websocketCommunicator.send(new Gson().toJson(command, UserGameCommand.class));
        websocketCommunicator.close();
    }

    private void verifyWebsocketExists() throws Exception {
        if (websocketCommunicator == null) {
            throw new Exception("Error: No websocket session");
        }
    }
}
