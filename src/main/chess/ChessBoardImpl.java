package chess;

import java.util.HashMap;
import java.util.Map;

public class ChessBoardImpl implements ChessBoard {

    private static final Map<ChessPiece.PieceType, String> pieceCharacters = new HashMap<>();
    static {
        pieceCharacters.put(ChessPiece.PieceType.KING, "k");
        pieceCharacters.put(ChessPiece.PieceType.QUEEN, "q");
        pieceCharacters.put(ChessPiece.PieceType.BISHOP, "b");
        pieceCharacters.put(ChessPiece.PieceType.KNIGHT, "n");
        pieceCharacters.put(ChessPiece.PieceType.ROOK, "r");
        pieceCharacters.put(ChessPiece.PieceType.PAWN, "p");
    };

    private final ChessPiece[][] boardSpaces = new ChessPieceImpl[8][8];

    @Override
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (getPiece(position) == null) {
            boardSpaces[rowToArrayIndex(position.getRow())][columnToArrayIndex(position.getColumn())] = piece;
        }
    }

    @Override
    public void movePiece(ChessMove move) {
        var fromRowIndex = rowToArrayIndex(move.getStartPosition().getRow());
        var fromColIndex = columnToArrayIndex(move.getStartPosition().getColumn());
        var toRowIndex = rowToArrayIndex(move.getEndPosition().getRow());
        var toColIndex = columnToArrayIndex(move.getEndPosition().getColumn());
        var movingPiece = boardSpaces[fromRowIndex][fromColIndex];
        if (movingPiece == null)
            throw new RuntimeException("Tried to move nothing (empty space)");
        var teamColor = movingPiece.getTeamColor();
        if (move.getPromotionPiece() != null) {
            boardSpaces[toRowIndex][toColIndex] = switch(move.getPromotionPiece()) {
                case KING -> new King(teamColor);
                case QUEEN -> new Queen(teamColor);
                case BISHOP -> new Bishop(teamColor);
                case KNIGHT -> new Knight(teamColor);
                case ROOK -> new Rook(teamColor);
                case PAWN -> new Pawn(teamColor);
            };
        }
        else
            boardSpaces[toRowIndex][toColIndex] = boardSpaces[fromRowIndex][fromColIndex];
        boardSpaces[fromRowIndex][fromColIndex] = null;
    }

    @Override
    public ChessPiece getPiece(ChessPosition position) {
        return boardSpaces[rowToArrayIndex(position.getRow())][columnToArrayIndex(position.getColumn())];
    }

    @Override
    public ChessPosition findKing(ChessGame.TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var position = new ChessPositionImpl(row, col);
                ChessPiece piece = getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING
                        && piece.getTeamColor() == teamColor)
                    return position;
            }
        }
        return null;
    }

    private int rowToArrayIndex(int row) {
        return 8 - row;
    }

    private int columnToArrayIndex(int column) {
        return column - 1;
    }

    @Override
    public void resetBoard() {
        // Clear the board
        for (int rowIndex = 0; rowIndex < 8; rowIndex++) {
            for (int colIndex = 0; colIndex < 8; colIndex++) {
                boardSpaces[rowIndex][colIndex] = null;
            }
        }

        // Add pieces at default positions
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPositionImpl(row, col);
                ChessGame.TeamColor teamColor = row > 2 ? (ChessGame.TeamColor.BLACK):(ChessGame.TeamColor.WHITE);
                boolean isStartRow = row < 3 || row > 6;
                boolean isPawnRow = row == 2 || row == 7;
                if (isStartRow) {
                    if (isPawnRow)
                        addPiece(position, new Pawn(teamColor));
                    else if (col == 1 || col == 8) {
                        addPiece(position, new Rook(teamColor));
                    }
                    else if (col == 2 || col == 7) {
                        addPiece(position, new Knight(teamColor));
                    }
                    else if (col == 3 || col == 6) {
                        addPiece(position, new Bishop(teamColor));
                    }
                    else if (col == 4) {
                        addPiece(position, new Queen(teamColor));
                    }
                    else // col == 5
                        addPiece(position, new King(teamColor));
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int row = 8; row >= 1; row--) {
            stringBuilder.append('|');
            for (int col = 1; col <= 8; col++) {
                var piece = getPiece(new ChessPositionImpl(row, col));
                if (piece != null) {
                    var pieceCharacter = pieceCharacters.get(piece.getPieceType());
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        pieceCharacter = pieceCharacter.toUpperCase();
                    stringBuilder.append(pieceCharacter);
                }
                else
                    stringBuilder.append(' ');
                stringBuilder.append('|');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
