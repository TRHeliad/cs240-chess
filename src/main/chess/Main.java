package chess;

import java.io.IOException;

/**
 * A simple main class for running the spelling corrector. This class is not
 * used by the passoff program.
 */
public class Main {

    /**
     * Give the dictionary file name as the first argument and the word to correct
     * as the second argument.
     */
    public static void main(String[] args) throws IOException {
        ChessBoard board = new ChestBoardImpl();
        board.resetBoard();
        System.out.println(board);
    }

}
