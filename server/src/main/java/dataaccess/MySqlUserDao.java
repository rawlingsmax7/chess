package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;


public class MySqlUserDao implements UserDao {
    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM user";
        try (Connection connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(sql);) {
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't clear users: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public void storeUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setString(1, user.username());

            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Can't store user: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email from user WHERE username = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);) {
            statement.setString(1, username);
            var resultSet = statement.executeQuery();
            // points to first row initially
            while (resultSet.next()) {
                String foundUsername = resultSet.getString("username");
                String foundPassword = resultSet.getString("password");
                String foundEmail = resultSet.getString("email");
                return new UserData(foundUsername, foundPassword, foundEmail);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Can't get user: " + exception.getMessage(),
                    exception);
        }
        // misses; return null
        return null;
    }
}
