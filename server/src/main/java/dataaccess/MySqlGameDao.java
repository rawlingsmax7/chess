package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDao implements GameDao {
    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't clear games: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public int storeGame(GameData game) throws DataAccessException {
        // don't need the gameID since it's on auto increment
        String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) " +
                "VALUES(?, ?, ?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, new Gson().toJson(game.game()));
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1); // gameID
        } catch (SQLException exception) {
            throw new DataAccessException("Can't store game: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game from game WHERE" +
                " gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setInt(1, gameID);
            var resultSet = statement.executeQuery();
            // points to first row initially
            while (resultSet.next()) {
                int foundGameID = resultSet.getInt("gameID");
                String foundWhiteUsername = resultSet.getString("whiteUsername");
                String foundBlackUsername = resultSet.getString("blackUsername");
                String foundgameName = resultSet.getString("gameName");
                ChessGame chessGame = new Gson().fromJson(resultSet.getString("game"),
                        ChessGame.class);
                return new GameData(foundGameID, foundWhiteUsername, foundBlackUsername,
                        foundgameName, chessGame);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Can't get game: " + exception.getMessage(),
                    exception);
        }
        // misses; return null
        return null;
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        ArrayList<GameData> chessGames = new ArrayList<GameData>();
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game from game";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            var resultSet = statement.executeQuery();
            // points to first row initially
            while (resultSet.next()) {
                int foundGameID = resultSet.getInt("gameID");
                String foundWhiteUsername = resultSet.getString("whiteUsername");
                String foundBlackUsername = resultSet.getString("blackUsername");
                String foundgameName = resultSet.getString("gameName");
                ChessGame chessGame = new Gson().fromJson(resultSet.getString("game"),
                        ChessGame.class);
                chessGames.add(new GameData(foundGameID, foundWhiteUsername, foundBlackUsername,
                        foundgameName, chessGame));
            }
            return chessGames;
        } catch (SQLException exception) {
            throw new DataAccessException("Can't get game list: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = " +
                "? WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, new Gson().toJson(game.game()));
            statement.setInt(5, game.gameID());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't update game: " + exception.getMessage(),
                    exception);
        }
    }
}
