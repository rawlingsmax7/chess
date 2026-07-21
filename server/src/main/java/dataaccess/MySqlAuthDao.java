package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySqlAuthDao implements AuthTokenDao {
    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't clear authentications: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public void storeAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't store authentication: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username from auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setString(1, authToken);
            var resultSet = statement.executeQuery();
            // points to first row initially
            while (resultSet.next()) {
                String foundAuthToken = resultSet.getString("authToken");
                String foundUsername = resultSet.getString("username");
                return new AuthData(foundAuthToken, foundUsername);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Can't get authentication: " + exception.getMessage(),
                    exception);
        }
        // misses; return null
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setString(1, auth.authToken());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't delete authentication: " + exception.getMessage(),
                    exception);
        }
    }
}
