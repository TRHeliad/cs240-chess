package chess;

import java.util.ArrayList;
import java.util.Collection;

public class Queen extends ChessPieceImpl{
    public Queen(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.QUEEN;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        // Go through each of the diagonal directions
        for (int rowDirection = -1; rowDirection <= 1; rowDirection += 2) {
            for (int colDirection = -1; colDirection <= 1; colDirection += 2) {
                for (int distance = 1; distance <= 7; distance ++) {
                    var row = myPosition.getRow() + rowDirection * distance;
                    var col = myPosition.getColumn() + colDirection * distance;
                    if (row > 0 && row < 9 && col > 0 && col < 9) {
                        ChessPosition targetPosition = new ChessPositionImpl(row, col);
                        ChessPiece blockingPiece = board.getPiece(targetPosition);
                        if (blockingPiece == null) {
                            moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                            continue;
                        } else if (blockingPiece.getTeamColor() != getTeamColor()) // can capture piece
                            moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                        break;
                    }
                }
            }
        }

        // Go through each of the straight directions
        for (int axis = 0; axis < 2; axis++) {
            for (int direction = -1; direction <= 1; direction += 2) {
                var rowDirection = axis == 0 ? direction:0;
                var colDirection = axis == 1 ? direction:0;
                for (int distance = 1; distance <= 7; distance++) {
                    var row = myPosition.getRow() + rowDirection * distance;
                    var col = myPosition.getColumn() + colDirection * distance;
                    if (row > 0 && row < 9 && col > 0 && col < 9) {
                        ChessPosition targetPosition = new ChessPositionImpl(row, col);
                        ChessPiece blockingPiece = board.getPiece(targetPosition);
                        if (blockingPiece == null) {
                            moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                            continue;
                        } else if (blockingPiece.getTeamColor() != getTeamColor()) // can capture piece
                            moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                        break;
                    }
                }
            }
        }

        return moves;
    }
}
