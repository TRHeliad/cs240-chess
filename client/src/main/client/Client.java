package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPositionImpl;
import model.Game;
import serverFacade.ServerFacade;
import webRequest.*;

import java.util.*;

enum ClientContext {
    PRELOGIN,
    POSTLOGIN
}
public class Client {
    private static final Map<ChessPiece.PieceType, String> pieceCharacters = new HashMap<>();
    static {
        pieceCharacters.put(ChessPiece.PieceType.KING, "K");
        pieceCharacters.put(ChessPiece.PieceType.QUEEN, "Q");
        pieceCharacters.put(ChessPiece.PieceType.BISHOP, "B");
        pieceCharacters.put(ChessPiece.PieceType.KNIGHT, "N");
        pieceCharacters.put(ChessPiece.PieceType.ROOK, "R");
        pieceCharacters.put(ChessPiece.PieceType.PAWN, "P");
    }

    ServerFacade serverFacade = new ServerFacade("localhost", "8080");
    private final Map<String, Command> commands = new HashMap<>();
    private boolean clientQuit = false;
    private String authToken;
    private Map<Integer, Game> gameList;

    public static void main(String[] args) { new Client().run(); }

    private ClientContext clientContext = ClientContext.PRELOGIN;
    private void run() {
        // Add commands
        var helpCommand = new Command("help - with possible commands") {
            public void run(String[] args) {
                for (Map.Entry<String, Command> entry : commands.entrySet()) {
                    if (entry.getValue().currentlyValid(clientContext))
                        System.out.println("\t" + entry.getValue().getHelpText());
                }
            }
        };
        helpCommand.addValidStatus(ClientContext.POSTLOGIN);
        helpCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("help", helpCommand);

        var quitCommand = new Command("quit - playing chess") {
            public void run(String[] args) {
                clientQuit = true;
                if (authToken != null) {
                    var logoutRequest = new LogoutRequest(authToken);
                    serverFacade.logout(logoutRequest);
                }
            }
        };
        quitCommand.addValidStatus(ClientContext.POSTLOGIN);
        quitCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("quit", quitCommand);

        var registerCommand = new Command("register <USERNAME> <PASSWORD> <EMAIL> - to create account") {
            public void run(String[] args) {
                var registerRequest = new RegisterRequest(args[0], args[1], args[2]);
                var registerResult = serverFacade.register(registerRequest);
                if (serverFacade.getStatusCode() == 200) {
                    authToken = registerResult.authToken();
                    clientContext = ClientContext.POSTLOGIN;
                    System.out.println("Logged in as " + registerResult.username());
                } else
                    System.out.println(registerResult.message());
            }
        };
        registerCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("register", registerCommand);

        var loginCommand = new Command("login <USERNAME> <PASSWORD> - to play chess") {
            public void run(String[] args) {
                var loginRequest = new LoginRequest(args[0], args[1]);
                var loginResult = serverFacade.login(loginRequest);
                if (serverFacade.getStatusCode() == 200) {
                    authToken = loginResult.authToken();
                    clientContext = ClientContext.POSTLOGIN;
                    System.out.println("Logged in as " + loginRequest.username());
                } else
                    System.out.println(loginResult.message());
            }
        };
        loginCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("login", loginCommand);

        var logoutCommand = new Command("logout - of account") {
            public void run(String[] args) {
                var logoutRequest = new LogoutRequest(authToken);
                var logoutResult = serverFacade.logout(logoutRequest);
                if (serverFacade.getStatusCode() == 200) {
                    authToken = null;
                    clientContext = ClientContext.PRELOGIN;
                } else
                    System.out.println(logoutResult.message());
            }
        };
        logoutCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("logout", logoutCommand);

        var createGameCommand = new Command("create <NAME> - a chess game") {
            public void run(String[] args) {
                var createGameRequest = new CreateGameRequest(args[0]);
                var createGameResult = serverFacade.createGame(createGameRequest, authToken);
                if (serverFacade.getStatusCode() == 200)
                    System.out.println("Created new game with name: "+ createGameRequest.gameName());
                else
                    System.out.println(createGameResult.message());
            }
        };
        createGameCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("create", createGameCommand);

        var listGamesCommand = new Command("list - games") {
            public void run(String[] args) {
                var listGamesResult = serverFacade.listGames(authToken);
                if (serverFacade.getStatusCode() == 200) {
                    var count = 0;
                    System.out.println("Number: Name, White Player, Black Player");
                    gameList = new HashMap<>();
                    for (Game game : listGamesResult.games()) {
                        var number = ++count;
                        gameList.put(number, game);
                        System.out.print(number + ": " + game.gameName() + ", ");
                        System.out.println(game.whiteUsername() + ", " + game.blackUsername());
                    }
                }
                else
                    System.out.println(listGamesResult.message());
            }
        };
        listGamesCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("list", listGamesCommand);

        var joinGameCommand = new Command("join <ID> [WHITE|BLACK|<empty>] - a game") {
            public void run(String[] args) {
                if (gameList == null) {
                    System.out.println("Use 'list' to get game numbers first");
                    return;
                }

                ChessGame.TeamColor teamColor = null;
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("WHITE"))
                        teamColor = ChessGame.TeamColor.WHITE;
                    if (args[1].equalsIgnoreCase("BLACK"))
                        teamColor = ChessGame.TeamColor.BLACK;
                }

                var number = Integer.parseInt(args[0]);
                var game = gameList.get(number);
                int gameID = -1;
                if (game != null) {
                    gameID = game.gameID();
                } else {
                    System.out.println("Invalid game number");
                    return;
                }

                var joinGameRequest = new JoinGameRequest(teamColor, gameID);
                var joinGameResult = serverFacade.joinGame(joinGameRequest, authToken);

                if (serverFacade.getStatusCode() == 200) {
                    System.out.println("Joined game with ID: " + joinGameRequest.gameID());
                    ChessBoard board = joinGameResult.game().game().getBoard();
                    System.out.println(createBoardDisplayString(board, true));
                    System.out.println(createBoardDisplayString(board, false));
                } else
                    System.out.println(joinGameResult.message());
            }
        };
        joinGameCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("join", joinGameCommand);

        runRepl();
    }

    private void runRepl() {
        System.out.println("Welcome to the chess game! Type 'help' to list available commands.");
        while (!clientQuit)  {
            System.out.print("[" + clientContext.toString() + "] >>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            var args = line.split(" ");
            var commandText = args[0].toLowerCase();
            var commandArgs = Arrays.copyOfRange(args, 1, args.length);
            var command = commands.get(commandText);

            if (command != null) {
                command.run(commandArgs);
            } else {
                System.out.println("Invalid command");
            }
        }
    }

    private String createBoardDisplayString(ChessBoard board, boolean isWhitePerspective) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\033[30;47;1m");
        stringBuilder.append("    a  b  c  d  e  f  g  h    \033[0m\n");
        var startRow = isWhitePerspective ? 8 : 1;
        var rowIncrement = isWhitePerspective ? -1 : 1;
        for (int row = startRow; isWhitePerspective ? (row >= 1) : (row <= 8); row += rowIncrement) {
            stringBuilder.append("\033[30;47;1m");
            stringBuilder.append(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                if ((col + row) % 2 == 0)
                    stringBuilder.append("\033[40m");
                else
                    stringBuilder.append("\033[107m");

                var piece = board.getPiece(new ChessPositionImpl(row, col));
                if (piece != null) {
                    var pieceCharacter = pieceCharacters.get(piece.getPieceType());
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        stringBuilder.append("\033[34m");
                    else
                        stringBuilder.append("\033[31m");
                    stringBuilder.append(" " + pieceCharacter + " ");
                }
                else
                    stringBuilder.append("   ");
            }
            stringBuilder.append("\033[30;47;1m");
            stringBuilder.append(" " + row + " ");
            stringBuilder.append("\033[0m");
            stringBuilder.append('\n');
        }
        stringBuilder.append("\033[30;47;1m");
        stringBuilder.append("    a  b  c  d  e  f  g  h    \033[0m\n");
        return stringBuilder.toString();
    }
}
