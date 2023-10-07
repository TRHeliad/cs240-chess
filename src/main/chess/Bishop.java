package chess;

import java.util.ArrayList;
import java.util.Collection;

public class Bishop extends ChessPieceImpl{
    public Bishop(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.BISHOP;
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

        return moves;
    }
}
