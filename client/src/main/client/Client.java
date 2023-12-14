package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPositionImpl;
import model.Game;
import serverFacade.ServerFacade;
import serverFacade.ServerMessageObserver;
import webRequest.*;
import webSocketMessages.serverMessages.ServerMessage;

import java.util.*;

public class Client {
    private final Repl repl;

    ServerMessageObserver serverMessageObserver = new ServerMessageObserver() {
        public void notify(ServerMessage message) {
            switch(message.getServerMessageType()) {
                case LOAD_GAME -> loadGame(message.getGame());
                case ERROR -> System.out.println("\nServerError: " + message.getErrorMessage());
                case NOTIFICATION -> System.out.println("\n" + message.getMessage());
            }
            System.out.print("[" + repl.getCurrentContext().toString() + "] >>> ");
        }
    };

    public static void main(String[] args) { new Client(); }

    public Client() {
        ServerFacade serverFacade = new ServerFacade("localhost", "8080", serverMessageObserver);
        repl = new Repl(serverFacade);
        repl.run();
    }

    private void loadGame(ChessGame game) {
        repl.setCurrentGame(game);
        System.out.println("\n" + repl.createBoardDisplayString(game.getBoard(), null));
    }
}
