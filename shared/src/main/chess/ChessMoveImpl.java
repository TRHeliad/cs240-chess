package chess;

import java.util.Objects;

public class ChessMoveImpl implements ChessMove {
    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType piecePromotionType;

    public ChessMoveImpl(ChessPosition startPos, ChessPosition endPos, ChessPiece.PieceType promotionType) {
        startPosition = startPos;
        endPosition = endPos;
        piecePromotionType = promotionType;
    }

    @Override
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    @Override
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    @Override
    public ChessPiece.PieceType getPromotionPiece() {
        return piecePromotionType;
    }

    @Override
    public String toString() {
        return startPosition.toString() + "->" + endPosition.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessMoveImpl chessMove = (ChessMoveImpl) o;
        return Objects.equals(startPosition, chessMove.startPosition)
                && Objects.equals(endPosition, chessMove.endPosition)
                && piecePromotionType == chessMove.piecePromotionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, piecePromotionType);
    }
}
