package chess;
import java.lang.Math;
import java.util.Objects;

public class ChessPositionImpl implements ChessPosition {

    private int row;
    private int column;

    public ChessPositionImpl(int row, int col) {
        // Clamp both row and column to the range [1,8]
        this.row = Math.max(1, Math.min(8, row));
        this.column = Math.max(1, Math.min(8, col));
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return ""+(char)(column-1+'a') + row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPositionImpl that = (ChessPositionImpl) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}
