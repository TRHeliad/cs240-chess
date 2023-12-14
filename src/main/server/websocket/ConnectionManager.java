package server.websocket;

import chess.ChessGameImpl;
import com.google.gson.Gson;
import model.Game;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public ConcurrentHashMap<Integer, ArrayList<Session>> gameMembers = new ConcurrentHashMap<>();
    private static final Gson gameAdapter = ChessGameImpl.getGsonAdapter();

    public void add(Integer gameID, Session session) {
        if (!gameMembers.containsKey(gameID))
            gameMembers.put(gameID, new ArrayList<>());
        gameMembers.get(gameID).add(session);
    }

    public void remove(Integer gameID, Session session) {
        if (gameMembers.containsKey(gameID)) {
            var members = gameMembers.get(gameID);
            members.remove(session);
        }
    }

    public void clear() {
        for (ArrayList<Session> members : gameMembers.values())
            for (Session s : members)
                if (s.isOpen())
                    s.close();
        gameMembers = new ConcurrentHashMap<>();
    }

    public void broadcastGameMessage(Game game, ServerMessage message, Session exceptSession) throws IOException {
        var members = gameMembers.get(game.gameID());
        if (members != null) {
            for (var s : members)
                if (!s.equals(exceptSession))
                    s.getRemote().sendString(gameAdapter.toJson(message));
        }
    }
}
