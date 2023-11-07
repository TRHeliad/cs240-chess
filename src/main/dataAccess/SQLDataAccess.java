package dataAccess;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import model.AuthToken;
import model.Game;
import model.User;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SQLDataAccess implements DataAccess{

    private static Database database = new Database();

    private static final SQLDataAccess sqlDataAccess = new SQLDataAccess();

    public static SQLDataAccess getInstance() { return sqlDataAccess; }

    private Gson gameSerializer;

    public SQLDataAccess() {
        try {
            database.initialize();
        } catch (DataAccessException exception) {
            throw new RuntimeException(exception.getMessage());
        }

        final RuntimeTypeAdapterFactory<ChessGame> gameTypeFactory = RuntimeTypeAdapterFactory
                .of(ChessGame.class, "type")
                .registerSubtype(ChessGameImpl.class);

        final RuntimeTypeAdapterFactory<ChessPiece> pieceTypeFactory = RuntimeTypeAdapterFactory
                .of(ChessPiece.class, "type")
                .registerSubtype(King.class)
                .registerSubtype(Knight.class)
                .registerSubtype(Pawn.class)
                .registerSubtype(Queen.class)
                .registerSubtype(Rook.class);

        final RuntimeTypeAdapterFactory<ChessBoard> boardTypeFactory = RuntimeTypeAdapterFactory
                .of(ChessBoard.class, "type")
                .registerSubtype(ChessBoardImpl.class);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(gameTypeFactory);
        builder.registerTypeAdapterFactory(pieceTypeFactory);
        builder.registerTypeAdapterFactory(boardTypeFactory);
        gameSerializer = builder.create();
    }

    public static void main(String[] args) {
        getInstance();
    }

    @Override
    public void createUser(User user) throws DataAccessException {
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM user WHERE username=?");
            preparedStatement.setString(1, user.username());
            var rs = preparedStatement.executeQuery();
            rs.next();
            var count = rs.getInt(1);
            if (count > 0)
                throw new DataAccessException("already taken");
            var insertString = "INSERT INTO user (username, password, email) VALUES(?, ?, ?)";
            var insertStatement = connection.prepareStatement(insertString);
            insertStatement.setString(1, user.username());
            insertStatement.setString(2, user.password());
            insertStatement.setString(3, user.email());
            insertStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public User getUser(String username) throws DataAccessException {
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT username, password, email FROM user WHERE username=?");
            preparedStatement.setString(1, username);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
        return null;
    }

    @Override
    public void destroyUser(User user) throws DataAccessException {
        if (user.username() == null)
            throw new DataAccessException("Username was null");
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("DELETE FROM user WHERE username=?");
            preparedStatement.setString(1, user.username());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public void createAuthToken(AuthToken authToken) throws DataAccessException {
        if (authToken.username() == null)
            throw new DataAccessException("Username was null");
        try(var connection = database.getConnection()) {
            var insertString = "INSERT INTO auth_token (token, username) VALUES(?, ?)";
            var insertStatement = connection.prepareStatement(insertString);
            insertStatement.setString(1, authToken.authToken());
            insertStatement.setString(2, authToken.username());
            insertStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public AuthToken getAuthToken(String authToken) throws DataAccessException {
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT token, username FROM auth_token WHERE token=?");
            preparedStatement.setString(1, authToken);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new AuthToken(
                        authToken,
                        rs.getString("username")
                );
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
        return null;
    }

    @Override
    public void destroyAuth(String authToken) throws DataAccessException {
        if (authToken == null)
            throw new DataAccessException("authToken was null");
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("DELETE FROM auth_token WHERE token=?");
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public Integer createGame(Game game) throws DataAccessException {
        if (game.gameName() == null)
            throw new DataAccessException("gameName was null");
        if (game.game() == null)
            throw new DataAccessException("game was null");
        try(var connection = database.getConnection()) {
            var insertString = "INSERT INTO game (white_username, black_username, game_name, chess_game) VALUES(?, ?, ?, ?)";
            var insertStatement = connection.prepareStatement(insertString, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, game.whiteUsername());
            insertStatement.setString(2, game.blackUsername());
            insertStatement.setString(3, game.gameName());
            var gameJSON = gameSerializer.toJson(game.game());
            insertStatement.setString(4, gameJSON);
            insertStatement.executeUpdate();

            var resultSet = insertStatement.getGeneratedKeys();
            var gameID = 0;
            if (resultSet.next()) {
                gameID = resultSet.getInt(1);
            }
            return gameID;
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    private boolean gameExists(int gameID) throws DataAccessException {
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM game WHERE id=?");
            preparedStatement.setInt(1, gameID);
            var rs = preparedStatement.executeQuery();
            rs.next();
            var count = rs.getInt(1);
            return count > 0;
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        try(var connection = database.getConnection()) {
            if (!gameExists(game.gameID()))
                throw new DataAccessException("bad request");

            var statement = "UPDATE game SET white_username=?, black_username=?, game_name=?, chess_game=? WHERE id=?";
            var preparedStatement = connection.prepareStatement(statement);

            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            var gameJSON = gameSerializer.toJson(game.game());
            preparedStatement.setString(4, gameJSON);

            preparedStatement.setInt(5, game.gameID());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public Collection<Game> getGames() throws DataAccessException {
        ArrayList<Game> games = new ArrayList<>();

        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT * FROM game");
            var rs = preparedStatement.executeQuery();
            while(rs.next()) {
                Game game = new Game(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        null
                );
                games.add(game);
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }

        return games;
    }

    @Override
    public Game getGame(Integer gameID) throws DataAccessException {
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT * FROM game WHERE id=?");
            preparedStatement.setInt(1, gameID);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new Game(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        gameSerializer.fromJson(rs.getString("chess_game"), ChessGameImpl.class)
                );
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
        throw new DataAccessException("bad request");
    }

    @Override
    public void joinGame(User user, ChessGame.TeamColor userColor, Game game) throws DataAccessException {
        try(var connection = database.getConnection()) {
            if (!gameExists(game.gameID()))
                throw new DataAccessException("bad request");

            var preparedStatement = connection.prepareStatement("SELECT * FROM game where id=?");
            preparedStatement.setInt(1, game.gameID());
            var rs = preparedStatement.executeQuery();
            rs.next();

            var updateString = "UPDATE game SET white_username=?, black_username=?, game_name=?, chess_game=? WHERE id=?";
            var updateStatement = connection.prepareStatement(updateString);
            var isWhite = userColor == ChessGame.TeamColor.WHITE;
            updateStatement.setString(1, isWhite ? user.username() : game.whiteUsername());
            updateStatement.setString(2, !isWhite ? user.username() : game.blackUsername());
            updateStatement.setString(3, game.gameName());
            updateStatement.setString(4, rs.getString("chess_game"));
            updateStatement.setInt(5, game.gameID());
            updateStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public void destroyGame(Integer gameID) throws DataAccessException {
        if (gameID == null)
            throw new DataAccessException("gameID was null");
        try(var connection = database.getConnection()) {
            var preparedStatement = connection.prepareStatement("DELETE FROM game WHERE id=?");
            preparedStatement.setInt(1, gameID);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    @Override
    public void clearData() throws DataAccessException {
        try(var connection = database.getConnection()) {
            var userClearStatement = connection.prepareStatement("TRUNCATE TABLE user");
            userClearStatement.executeUpdate();
            var gameClearStatement = connection.prepareStatement("TRUNCATE TABLE game");
            gameClearStatement.executeUpdate();
            var authClearStatement = connection.prepareStatement("TRUNCATE TABLE auth_token");
            authClearStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    private ChessGame parseGameJSON(String gameJSON) {
        return null;
    }
}
