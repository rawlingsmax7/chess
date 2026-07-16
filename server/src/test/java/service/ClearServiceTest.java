package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {
    private ClearService clearService;
    private UserDao userDao;
    private GameDao gameDao;
    private AuthTokenDao authDao;

    @BeforeEach
    public void setupTest() {
        userDao = new MemoryUserDao();
        gameDao = new MemoryGameDao();
        authDao = new MemoryAuthTokenDao();
        clearService = new ClearService(userDao, gameDao, authDao);
    }

    // positive clear test, don't need to show negative case
    @Test
    public void clearSuccess() throws DataAccessException {
        // populate memory with all of the different model classes
        userDao.storeUser(new UserData("Max", "password", "max@gmail.com"));
        authDao.storeAuth(new AuthData("basic token", "Max"));
        gameDao.storeGame(new GameData(1, null, null, "Pro Game", new ChessGame()));

        clearService.clear();

        // ensure that everything is gone
        Assertions.assertNull(userDao.getUser("Max"));
        Assertions.assertNull(authDao.getAuth("basic token"));
        Assertions.assertTrue(gameDao.getGames().isEmpty());
    }
}
