package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Database is responsible for creating connections to the database. Connections are
 * managed with a simple pool in order to increase performance. To obtain and
 * use connections represented by this class use the following pattern.
 *
 * <pre>
 *  public boolean example(String selectStatement, Database db) throws DataAccessException{
 *    var conn = db.getConnection();
 *    try (var preparedStatement = conn.prepareStatement(selectStatement)) {
 *        return preparedStatement.execute();
 *    } catch (SQLException ex) {
 *        throw new DataAccessException(ex.toString());
 *    } finally {
 *        db.returnConnection(conn);
 *    }
 *  }
 * </pre>
 */
public class Database {

    // FIXME: Change these fields, if necessary, to match your database configuration
    public static final String DB_NAME = "chess";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "abc123";

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306";

    private final LinkedList<Connection> connections = new LinkedList<>();

    /**
     * Get a connection to the database. This pulls a connection out of a simple
     * pool implementation. The connection must be returned to the pool after
     * you are done with it by calling {@link #returnConnection(Connection) returnConnection}.
     *
     * @return Connection
     */
    synchronized public Connection getConnection() throws DataAccessException {
        try {
            Connection connection;
            if (connections.isEmpty()) {
                connection = DriverManager.getConnection(CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
                connection.setCatalog(DB_NAME);
            } else {
                connection = connections.removeFirst();
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void initialize() throws DataAccessException {
        try {
            Connection connection;
            if (connections.isEmpty()) {
                connection = DriverManager.getConnection(CONNECTION_URL, DB_USERNAME, DB_PASSWORD);

                var createDbStatement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS chess");
                createDbStatement.executeUpdate();

                connection.setCatalog(DB_NAME);

                var createUserTable = """
                CREATE TABLE IF NOT EXISTS user (
                    id INT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    PRIMARY KEY (id)
                )""";

                var createAuthTokenTable = """
                CREATE TABLE IF NOT EXISTS auth_token (
                    token VARCHAR(36) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    PRIMARY KEY (token)
                )""";

                var createGameTable = """
                CREATE TABLE IF NOT EXISTS game (
                    id INT NOT NULL AUTO_INCREMENT,
                    white_username VARCHAR(255),
                    black_username VARCHAR(255),
                    game_name VARCHAR(255) NOT NULL,
                    chess_game longtext NOT NULL,
                    game_over BOOLEAN NOT NULL,
                    PRIMARY KEY (id)
                )""";

                var createTableStatement = connection.prepareStatement(createUserTable);
                createTableStatement.executeUpdate();

                createTableStatement = connection.prepareStatement(createAuthTokenTable);
                createTableStatement.executeUpdate();

                createTableStatement = connection.prepareStatement(createGameTable);
                createTableStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Return a previously acquired connection to the pool.
     *
     * @param connection previous obtained by calling {@link #getConnection() getConnection}.
     */
    synchronized public void returnConnection(Connection connection) {
        connections.add(connection);
    }
}

