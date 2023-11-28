package chess;

import java.util.Collection;

public abstract class ChessPieceImpl implements ChessPiece {
    private final ChessGame.TeamColor teamColor;
    protected PieceType type;

    public ChessPieceImpl(ChessGame.TeamColor color) {
        type = getPieceType();
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
