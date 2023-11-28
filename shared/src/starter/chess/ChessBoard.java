package chess;

/**
 * A chessboard that can hold and rearrange chess pieces
 */
public interface ChessBoard {

    /**
     * Adds a chess piece to the chessboard
     * @param position where to add the piece to
     * @param piece the piece to add
     */
    void addPiece(ChessPosition position, ChessPiece piece);

    /**
     * Gets a chess piece on the chessboard
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that position
     */
    ChessPiece getPiece(ChessPosition position);

    /**
     * Gets the position of a king
     * @param teamColor The color of the king
     * @return Either position of the king, or null if there is no king of that team
     */
    ChessPosition findKing(ChessGame.TeamColor teamColor);

    /**
     * Moves a chess piece on the chessboard
     * @param move The move to make
     */
    void movePiece(ChessMove move);

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    void resetBoard();

    /**
     * Returns a string representation of the board
     * @param isWhitePerspective the perspective to display
     */
    public String boardToString(boolean isWhitePerspective);
}
