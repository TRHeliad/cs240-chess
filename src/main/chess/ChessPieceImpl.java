package chess;

import java.util.Collection;

public abstract class ChessPieceImpl implements ChessPiece {
    private final ChessGame.TeamColor teamColor;

    public ChessPieceImpl(ChessGame.TeamColor color) {
        teamColor = color;
    }

    @Override
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    public abstract PieceType getPieceType();

    @Override
    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}
