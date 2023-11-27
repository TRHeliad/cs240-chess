package chess;

import java.util.ArrayList;
import java.util.Collection;

public class Knight extends ChessPieceImpl{
    public Knight(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.KNIGHT;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        for (int rowDirection = -2; rowDirection <= 2; rowDirection++) {
            for (int colDirection = -2; colDirection <= 2; colDirection++) {
                // An L doesn't have equal sides! (also skip 0s)
                if (Math.abs(rowDirection) != Math.abs(colDirection) && rowDirection != 0 && colDirection != 0) {
                    var row = myPosition.getRow() + rowDirection;
                    var col = myPosition.getColumn() + colDirection;
                    if (row > 0 && row < 9 && col > 0 && col < 9) {
                        ChessPosition targetPosition = new ChessPositionImpl(row, col);
                        ChessPiece blockingPiece = board.getPiece(targetPosition);
                        if (blockingPiece == null || blockingPiece.getTeamColor() != getTeamColor())
                            moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                    }
                }
            }
        }

        return moves;
    }
}
