package client;

import chess.*;
import model.Game;
import serverFacade.ServerFacade;
import serverFacade.ServerMessageObserver;
import webRequest.*;

import java.util.*;

public class Repl {
    public enum ClientContext {
        PRELOGIN,
        POSTLOGIN,
        OBSERVING,
        PLAYING
    }
    private static final Map<ChessPiece.PieceType, String> pieceCharacters = new HashMap<>();
    static {
        pieceCharacters.put(ChessPiece.PieceType.KING, "K");
        pieceCharacters.put(ChessPiece.PieceType.QUEEN, "Q");
        pieceCharacters.put(ChessPiece.PieceType.BISHOP, "B");
        pieceCharacters.put(ChessPiece.PieceType.KNIGHT, "N");
        pieceCharacters.put(ChessPiece.PieceType.ROOK, "R");
        pieceCharacters.put(ChessPiece.PieceType.PAWN, "P");
    }

    private final Map<String, Command> commands = new HashMap<>();
    private boolean clientQuit = false;
    private String authToken;
    private ChessGame.TeamColor playerColor;
    private Map<Integer, Game> gameList;
    private ClientContext clientContext = ClientContext.PRELOGIN;
    private ChessGame currentGame;
    private Integer currentGameID;

    public Repl(ServerFacade serverFacade) {
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
        helpCommand.addValidStatus(ClientContext.OBSERVING);
        helpCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("help", helpCommand);

        var quitCommand = new Command("quit - playing chess") {
            public void run(String[] args) throws Exception {
                clientQuit = true;
                if (authToken != null) {
                    if (clientContext == ClientContext.OBSERVING || clientContext == ClientContext.PLAYING)
                        serverFacade.leave(authToken, currentGameID);
                    var logoutRequest = new LogoutRequest(authToken);
                    serverFacade.logout(logoutRequest);
                }
            }
        };
        quitCommand.addValidStatus(ClientContext.POSTLOGIN);
        quitCommand.addValidStatus(ClientContext.PRELOGIN);
        quitCommand.addValidStatus(ClientContext.OBSERVING);
        quitCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("quit", quitCommand);

        var registerCommand = new Command("register <USERNAME> <PASSWORD> <EMAIL> - to create account") {
            public void run(String[] args) throws Exception {
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
            public void run(String[] args) throws Exception {
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
            public void run(String[] args) throws Exception {
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
            public void run(String[] args) throws Exception {
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
            public void run(String[] args) throws Exception {
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
            public void run(String[] args) throws Exception {
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
                    currentGameID = gameID;
                    System.out.println("Joined game with ID: " + gameID);
                    //ChessBoard board = joinGameResult.game().game().getBoard();
                    //System.out.println(createBoardDisplayString(board, true));
                    //System.out.println(createBoardDisplayString(board, false));
                    playerColor = teamColor;
                    clientContext = teamColor == null ? ClientContext.OBSERVING : ClientContext.PLAYING;
                } else
                    System.out.println(joinGameResult.message());
            }
        };
        joinGameCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("join", joinGameCommand);

        // Observer / Player commands

        var redrawBoardCommand = new Command("redraw - the board") {
            public void run(String[] args) {
                System.out.println(createBoardDisplayString(currentGame.getBoard(), null));
            }
        };
        redrawBoardCommand.addValidStatus(ClientContext.OBSERVING);
        redrawBoardCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("redraw", redrawBoardCommand);

        var leaveGameCommand = new Command("leave - game") {
            public void run(String[] args) throws Exception {
                serverFacade.leave(authToken, currentGameID);
                clientContext = ClientContext.POSTLOGIN;
                System.out.println("Left the game");
                playerColor = null;
                currentGameID = null;
            }
        };
        leaveGameCommand.addValidStatus(ClientContext.OBSERVING);
        leaveGameCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("leave", leaveGameCommand);

        var resignGameCommand = new Command("resign - games") {
            public void run(String[] args) throws Exception {
                serverFacade.resign(authToken, currentGameID);
                clientContext = ClientContext.POSTLOGIN;
                System.out.println("Resigned from the game");
                playerColor = null;
                currentGameID = null;
            }
        };
        resignGameCommand.addValidStatus(ClientContext.OBSERVING);
        resignGameCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("resign", resignGameCommand);

        var moveCommand = new Command("move - a piece") {
            public void run(String[] args) throws Exception {
                if (args.length > 1 && args[0] == null || args[1] == null)
                    System.out.println("Must enter a start and end position. e.g. 'a2 a3'");
                else {
                    var startPosition = getChessPositionFromString(args[0]);
                    var endPosition = getChessPositionFromString(args[1]);
                    ChessPiece.PieceType promotionType = null;
                    if (args.length > 2 && args[2] != null)
                        promotionType = getPieceTypeFromString(args[2]);

                    if (startPosition == null || endPosition == null)
                        System.out.println("Invalid position");
                    else
                        serverFacade.makeMove(new ChessMoveImpl(startPosition, endPosition, promotionType),
                                currentGameID, authToken);
                }
            }
        };
        moveCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("move", moveCommand);
        var showMovesCommand = new Command("showmoves - of piece") {
            public void run(String[] args) throws Exception {
                var piecePosition = getChessPositionFromString(args[0]);
                if (piecePosition == null)
                    System.out.println("Invalid position");
                else {
                    var validMoves = currentGame.validMoves(piecePosition);
                    if (validMoves == null)
                        System.out.println("Invalid position");
                    else {
                        boolean isWhite = playerColor == ChessGame.TeamColor.WHITE;
                        boolean isObserver = playerColor == null;
                        if (validMoves.isEmpty())
                            System.out.println("No valid moves");
                        else {
                            System.out.println(createBoardDisplayString(currentGame.getBoard(), validMoves));
                        }
                    }
                }
            }
        };
        showMovesCommand.addValidStatus(ClientContext.OBSERVING);
        showMovesCommand.addValidStatus(ClientContext.PLAYING);
        commands.put("showmoves", showMovesCommand);
    }

    public ClientContext getCurrentContext() {
        return clientContext;
    }

    public void setCurrentGame(ChessGame game) {
        currentGame = game;
    }

    public void run() {
        System.out.println("Welcome to the chess game! Type 'help' to list available commands.");
        while (!clientQuit)  {
            System.out.print("[" + clientContext.toString() + "] >>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            var args = line.split(" ");
            var commandText = args[0].toLowerCase();
            var commandArgs = Arrays.copyOfRange(args, 1, args.length);
            var command = commands.get(commandText);

            if (command != null && command.currentlyValid(clientContext)) {
                try {
                    command.run(commandArgs);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
            } else {
                System.out.println("Invalid command");
            }
        }
    }

    public String createBoardDisplayString(ChessBoard board, Collection<ChessMove> highlightMoves) {
        if (playerColor == null) {
            return createObserverBoardString(board, highlightMoves);
        } else {
            var isWhitePerspective = playerColor == ChessGame.TeamColor.WHITE;
            return createPlayerBoardString(board, isWhitePerspective, highlightMoves);
        }
    }

    private String createPlayerBoardString(ChessBoard board, boolean isWhitePerspective,
                                           Collection<ChessMove> highlightMoves) {
        ChessPosition highlightPiece = null;
        boolean[][] shouldHighlight = null;
        if (highlightMoves != null) {
            highlightPiece = highlightMoves.iterator().next().getStartPosition();
            shouldHighlight = new boolean[8][8]; // defaults to false
            for (ChessMove move : highlightMoves)
                shouldHighlight[move.getEndPosition().getRow()-1][move.getEndPosition().getColumn()-1] = true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\033[30;47;1m");

        var headerString = isWhitePerspective ? "    a  b  c  d  e  f  g  h    \033[0m\n" :
                "    h  g  f  e  d  c  b  a    \033[0m\n";
        stringBuilder.append(headerString);

        var startRow = isWhitePerspective ? 8 : 1;
        var rowIncrement = isWhitePerspective ? -1 : 1;
        var startCol = isWhitePerspective ? 1 : 8;
        var colIncrement = isWhitePerspective ? 1 : -1;

        for (int row = startRow; isWhitePerspective ? (row >= 1) : (row <= 8); row += rowIncrement) {
            stringBuilder.append("\033[30;47;1m");
            stringBuilder.append(" ").append(row).append(" ");
            for (int col = startCol; isWhitePerspective ? (col <= 8) : (col >= 1); col += colIncrement) {
                boolean highlightSquare = (shouldHighlight != null && shouldHighlight[row-1][col-1]);
                if (highlightPiece != null && highlightPiece.getRow() == row && highlightPiece.getColumn() == col)
                    stringBuilder.append("\033[103m");
                else if ((col + row) % 2 == 0)
                    stringBuilder.append("\033[").append(highlightSquare ? "42m" : "40m");
                else
                    stringBuilder.append("\033[").append(highlightSquare ? "102m" : "107m");

                var piece = board.getPiece(new ChessPositionImpl(row, col));
                if (piece != null) {
                    var pieceCharacter = pieceCharacters.get(piece.getPieceType());
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        stringBuilder.append("\033[34m");
                    else
                        stringBuilder.append("\033[31m");
                    stringBuilder.append(" ").append(pieceCharacter).append(" ");
                }
                else
                    stringBuilder.append("   ");
            }
            stringBuilder.append("\033[30;47;1m");
            stringBuilder.append(" ").append(row).append(" ");
            stringBuilder.append("\033[0m");
            stringBuilder.append('\n');
        }
        stringBuilder.append("\033[30;47;1m");
        stringBuilder.append(headerString);
        return stringBuilder.toString();
    }

    private String createObserverBoardString(ChessBoard board, Collection<ChessMove> highlightMoves) {
        ChessPosition highlightPiece = null;
        boolean[][] shouldHighlight = null;
        if (highlightMoves != null) {
            highlightPiece = highlightMoves.iterator().next().getStartPosition();
            shouldHighlight = new boolean[8][8]; // defaults to false
            for (ChessMove move : highlightMoves)
                shouldHighlight[move.getEndPosition().getRow()-1][move.getEndPosition().getColumn()-1] = true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\033[30;47;1m");
        stringBuilder.append("    1  2  3  4  5  6  7  8    \033[0m\n");
        for (int col = 1; col <= 8; col++) {
            stringBuilder.append("\033[30;47;1m");
            var colLetter = (char)(col + 96);
            stringBuilder.append(" ").append(colLetter).append(" ");
            for (int row = 1; row <= 8; row++) {
                boolean highlightSquare = (shouldHighlight != null && shouldHighlight[row-1][col-1]);
                if (highlightPiece != null && highlightPiece.getRow() == row && highlightPiece.getColumn() == col)
                    stringBuilder.append("\033[103m");
                else if ((col + row) % 2 == 0)
                    stringBuilder.append("\033[").append(highlightSquare ? "42m" : "40m");
                else
                    stringBuilder.append("\033[").append(highlightSquare ? "102m" : "107m");

                var piece = board.getPiece(new ChessPositionImpl(row, col));
                if (piece != null) {
                    var pieceCharacter = pieceCharacters.get(piece.getPieceType());
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        stringBuilder.append("\033[34m");
                    else
                        stringBuilder.append("\033[31m");
                    stringBuilder.append(" ").append(pieceCharacter).append(" ");
                }
                else
                    stringBuilder.append("   ");
            }
            stringBuilder.append("\033[30;47;1m");
            stringBuilder.append(" ").append(colLetter).append(" ");
            stringBuilder.append("\033[0m");
            stringBuilder.append('\n');
        }
        stringBuilder.append("\033[30;47;1m");
        stringBuilder.append("    1  2  3  4  5  6  7  8    \033[0m\n");
        return stringBuilder.toString();
    }

    private ChessPosition getChessPositionFromString(String positionString) {
        if (positionString.length() > 1) {
            int col = positionString.charAt(0) - 96;
            int row = positionString.charAt(1) - 48;
            if (col > 0 && col < 9 && row > 0 && row < 9)
                return new ChessPositionImpl(row, col);
        }
        return null;
    }

    private ChessPiece.PieceType getPieceTypeFromString(String pieceString) {
        if (!pieceString.isEmpty()) {
            return switch(pieceString.toLowerCase().charAt(0)) {
                case 'q' -> ChessPiece.PieceType.QUEEN;
                case 'k' -> ChessPiece.PieceType.KING;
                case 'b' -> ChessPiece.PieceType.BISHOP;
                case 'n' -> ChessPiece.PieceType.KNIGHT;
                case 'r' -> ChessPiece.PieceType.ROOK;
                case 'p' -> ChessPiece.PieceType.PAWN;
                default -> null;
            };
        }
        return null;
    }
}
