package service;

import dataaccess.AuthTokenDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.UserDao;

public class ClearService {
    private final UserDao userDao;
    private final GameDao gameDao;
    private final AuthTokenDao authDao;

    // constructor
    public ClearService(UserDao userDao, GameDao gameDao, AuthTokenDao authDao) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public void clear() throws DataAccessException {
        userDao.clear();
        gameDao.clear();
        authDao.clear();
    }
}
