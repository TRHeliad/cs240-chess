package chess;

import java.util.ArrayList;
import java.util.Collection;

public class King extends ChessPieceImpl{
    public King(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.KING;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        // Go through all directions
        for (int rowDirection = -1; rowDirection <= 1; rowDirection++) {
            for (int colDirection = -1; colDirection <= 1; colDirection++) {
                if (rowDirection == 0 && colDirection == 0)
                    continue;
                var row = myPosition.getRow() + rowDirection;
                var col = myPosition.getColumn() + colDirection;
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

        return moves;
    }
}
