package chess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.util.*;

public class ChessGameImpl implements ChessGame {
    private TeamColor currentTurnColor = TeamColor.WHITE;
    private ChessBoardImpl board;

    private static final Gson gameAdapter;
    static {
        final RuntimeTypeAdapterFactory<ChessGame> gameTypeFactory = RuntimeTypeAdapterFactory
                .of(ChessGame.class, "type")
                .registerSubtype(ChessGameImpl.class);

        final RuntimeTypeAdapterFactory<ChessPosition> chessPositionFactory = RuntimeTypeAdapterFactory
                .of(ChessPosition.class, "type")
                .registerSubtype(ChessPositionImpl.class);

        final RuntimeTypeAdapterFactory<ChessMove> chessMoveFactory = RuntimeTypeAdapterFactory
                .of(ChessMove.class, "type")
                .registerSubtype(ChessMoveImpl.class);

        final RuntimeTypeAdapterFactory<ChessBoard> boardTypeFactory = RuntimeTypeAdapterFactory
                .of(ChessBoard.class, "type")
                .registerSubtype(ChessBoardImpl.class);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(gameTypeFactory);
        builder.registerTypeAdapterFactory(chessPositionFactory);
        builder.registerTypeAdapterFactory(chessMoveFactory);
        builder.registerTypeAdapterFactory(boardTypeFactory);
        builder.registerTypeAdapter(ChessPiece.class, new ChessPieceAdapter());
        gameAdapter = builder.create();
    }
    public static Gson getGsonAdapter() { return gameAdapter; }

    @Override
    public TeamColor getTeamTurn() {
        return currentTurnColor;
    }

    @Override
    public void setTeamTurn(TeamColor team) {
        currentTurnColor = team;
    }

    @Override
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var piece = board.getPiece(startPosition);
        if (piece != null) {
            var pieceMoves = piece.pieceMoves(board, startPosition);
            ArrayList<ChessMove> invalidMoves = new ArrayList<>();
            for (ChessMove move : pieceMoves) {
                // Make hypothetical move
                var takenPiece = board.getPiece(move.getEndPosition());
                ((ChessBoardImpl)board).movePiece(move);

                var nowInCheck = isInCheck(piece.getTeamColor());

                // Undo hypothetical move
                board.movePiece(new ChessMoveImpl(
                        move.getEndPosition(),
                        move.getStartPosition(),
                        piece.getPieceType()
                ));
                if (takenPiece != null)
                    board.addPiece(move.getEndPosition(), takenPiece);

                if (nowInCheck)
                    invalidMoves.add(move);
            }
            pieceMoves.removeAll(invalidMoves);
            return pieceMoves;
        }
        return null;
    }

    @Override
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var piece = board.getPiece(move.getStartPosition());
        if (piece != null && getTeamTurn() != piece.getTeamColor())
            throw new InvalidMoveException("Not " + piece.getTeamColor() + "'s turn");

        var validMoves = validMoves(move.getStartPosition());
        if (validMoves == null)
            throw new InvalidMoveException("Empty space");
        else {
            Set<ChessMove> validMovesSet = new HashSet<>(validMoves);
            if (validMovesSet.contains(move)) {
                board.movePiece(move);
                setTeamTurn(currentTurnColor == TeamColor.WHITE ? TeamColor.BLACK:TeamColor.WHITE);
            }
            else
                throw new InvalidMoveException("Move not an option");
        }
    }

    @Override
    public boolean isInCheck(TeamColor teamColor) {
        var kingPosition = board.findKing(teamColor);
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var position = new ChessPositionImpl(row, col);
                var piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        if (move.getEndPosition().equals(kingPosition))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    var position = new ChessPositionImpl(row, col);
                    var piece = board.getPiece(position);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        for (ChessMove move : validMoves(position)) {
                            // Make hypothetical move
                            var takenPiece = board.getPiece(move.getEndPosition());
                            board.movePiece(move);

                            var stillInCheck = isInCheck(teamColor);

                            // Undo hypothetical move
                            board.movePiece(new ChessMoveImpl(
                                    move.getEndPosition(),
                                    move.getStartPosition(),
                                    piece.getPieceType()
                            ));
                            if (takenPiece != null)
                                board.addPiece(move.getEndPosition(), takenPiece);

                            if (!stillInCheck)
                                return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isInStalemate(TeamColor teamColor) {
        if (getTeamTurn() == teamColor) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    var position = new ChessPositionImpl(row, col);
                    var piece = board.getPiece(position);
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        var validMoves = validMoves(position);
                        if (!validMoves.isEmpty())
                            return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setBoard(ChessBoard board) {
        this.board = (ChessBoardImpl) board;
    }

    @Override
    public ChessBoard getBoard() {
        return board;
    }
}
