package chess;

import java.util.Collection;
import java.util.ArrayList;

public class Pawn extends ChessPieceImpl{
    private static final ChessPiece.PieceType[] promotionTypes = {
        PieceType.QUEEN,
        PieceType.BISHOP,
        PieceType.ROOK,
        PieceType.KNIGHT
    };

    public Pawn(ChessGame.TeamColor color) {
        super(color);
    }

    @Override
    public PieceType getPieceType() {
        return PieceType.PAWN;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int rowDirection = getTeamColor() == ChessGame.TeamColor.WHITE ? 1:-1;

        ChessPositionImpl singleMovePosition = new ChessPositionImpl(
                myPosition.getRow() + rowDirection,
                myPosition.getColumn()
        );

        // Add single space move
        if (board.getPiece(singleMovePosition) == null) {
            if (rowDirection == 1 && singleMovePosition.getRow() == 8
                    || rowDirection == -1 && singleMovePosition.getRow() == 1)
                addPromotionMoves(moves, myPosition, singleMovePosition);
            else
                moves.add(new ChessMoveImpl(myPosition, singleMovePosition, null));

            // Add double space move
            if (rowDirection == 1 && myPosition.getRow() == 2 || rowDirection == -1 && myPosition.getRow() == 7) {
                ChessPositionImpl doubleMovePosition = new ChessPositionImpl(
                        myPosition.getRow() + rowDirection * 2,
                        myPosition.getColumn()
                );
                if (board.getPiece(doubleMovePosition) == null)
                    moves.add(new ChessMoveImpl(myPosition, doubleMovePosition, null));
            }
        }

        // Check captures
        for (int colDirection = -1; colDirection <= 1; colDirection += 2) {
            var row = myPosition.getRow() + rowDirection;
            var col = myPosition.getColumn() + colDirection;
            if (row > 0 && row < 9 && col > 0 && col < 9) {
                ChessPosition targetPosition = new ChessPositionImpl(row, col);
                ChessPiece blockingPiece = board.getPiece(targetPosition);
                if (blockingPiece != null && blockingPiece.getTeamColor() != getTeamColor()) {
                    if (rowDirection == 1 && row == 8 || rowDirection == -1 && row == 1)
                        addPromotionMoves(moves, myPosition, targetPosition);
                    else
                        moves.add(new ChessMoveImpl(myPosition, targetPosition, null));
                }
            }
        }

        return moves;
    }

    private void addPromotionMoves(ArrayList<ChessMove> moves, ChessPosition startPos, ChessPosition endPos) {
        for (int i = 0; i < 4; i++) {
            moves.add(new ChessMoveImpl(startPos, endPos, promotionTypes[i]));
        }
    }
}
