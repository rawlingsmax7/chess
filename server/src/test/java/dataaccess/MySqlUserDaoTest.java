package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MySqlUserDaoTest {

    private MySqlUserDao userDao;

    @BeforeEach
    public void setupTest() throws DataAccessException {
        userDao = new MySqlUserDao();
        userDao.clear();          // fresh table before every test
    }

    // positive clear test
    @Test
    public void clearSuccess() throws DataAccessException {
        UserData user = new UserData("Max", "password", "max@gmail.com");
        userDao.storeUser(user);

        userDao.clear();
        UserData foundUser = userDao.getUser("Max");

        Assertions.assertNull(foundUser);
    }

    @Test
    public void storeUserSuccess() throws DataAccessException {
        UserData user = new UserData("Max", "password", "max@gmail.com");
        userDao.storeUser(user);

        UserData foundUser = userDao.getUser("Max");
        assertNotNull(foundUser);
        assertEquals("Max", foundUser.username());
        assertEquals("max@mail.com", foundUser.email());
        // the DAO stores the password, the service does the hashing now
        assertEquals("password", foundUser.password());
    }

    @Test
    // try to store the same username twice
    public void storeDuplicatedUser() throws DataAccessException {
        UserData user = new UserData("Max", "password", "max@gmail.com");
        userDao.storeUser(user);
        UserData repeatUser = new UserData("Max", "password", "max@gmail.com");

        Assertions.assertThrows(DataAccessException.class, () -> userDao.storeUser(repeatUser));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("John", "123", "john@gmail.com");
        userDao.storeUser(user);

        UserData foundUser = userDao.getUser("Max");
        assertEquals(user, foundUser);
    }

    @Test
    // try to get a User that wasn't stored
    public void getUserNotFound() throws DataAccessException {
        UserData foundUser = userDao.getUser("Max");
        Assertions.assertNull(foundUser);
    }

}
