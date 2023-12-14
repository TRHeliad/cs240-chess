package passoffTests;

import chess.*;
import dataAccess.DataAccess;
import dataAccess.SQLDataAccess;
import model.AuthToken;
import model.Game;
import model.User;
import service.ApplicationService;
import service.GameService;
import service.UserService;

/**
 * Used for testing your code
 * Add in code using your classes for each method for each FIXME
 */
public class TestFactory {

    //Chess Functions
    //------------------------------------------------------------------------------------------------------------------
    public static ChessBoard getNewBoard(){
		return new ChessBoardImpl();
    }

    public static ChessGame getNewGame(){
		return new ChessGameImpl();
    }

    public static ChessPiece getNewPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type){
        return switch (type) {
            case KING -> new King(pieceColor);
            case QUEEN -> new Queen(pieceColor);
            case BISHOP -> new Bishop(pieceColor);
            case KNIGHT -> new Knight(pieceColor);
            case ROOK -> new Rook(pieceColor);
            case PAWN -> new Pawn(pieceColor);
        };
    }

    public static ChessPosition getNewPosition(Integer row, Integer col){
		return new ChessPositionImpl(row, col);
    }

    public static ChessMove getNewMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece){
		return new ChessMoveImpl(startPosition, endPosition, promotionPiece);
    }
    //------------------------------------------------------------------------------------------------------------------


    //Server API's
    //------------------------------------------------------------------------------------------------------------------
    public static String getServerPort(){
        return "8080";
    }
    public static UserService getUserService() { return UserService.getInstance(); }
    public static GameService getGameService() { return GameService.getInstance(); }
    public static ApplicationService getApplicationService() { return ApplicationService.getInstance(); }
    public static DataAccess getDataAccess() { return SQLDataAccess.getInstance(); }
    public static User createSimpleUser() {
        return new User("jeff", "abc123", "jeff@gmail.com");
    }
    //------------------------------------------------------------------------------------------------------------------

    //Server Database
    //------------------------------------------------------------------------------------------------------------------
    public static Game createSimpleGame() {
        return new Game(0, "jeff", "max", "testGame", getNewGame(), false);
    }
    public static AuthToken createSimpleAuthToken() {
        var user = createSimpleUser();
        return new AuthToken("testToken", user.username());
    }
    //------------------------------------------------------------------------------------------------------------------

    //Websocket Tests
    //------------------------------------------------------------------------------------------------------------------
    public static Long getMessageTime(){
        /*
        Changing this will change how long tests will wait for the server to send messages.
        3000 Milliseconds (3 seconds) will be enough for most computers. Feel free to change as you see fit,
        just know increasing it can make tests take longer to run.
        (On the flip side, if you've got a good computer feel free to decrease it)
         */
        return 3000L;
    }
    //------------------------------------------------------------------------------------------------------------------
}
