package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlAuthDaoTest {

    private MySqlAuthDao authDao;

    @BeforeEach
    public void setupTest() throws DataAccessException {
        authDao = new MySqlAuthDao();

        authDao.clear();
    }

    // positive clear test
    @Test
    public void clearSuccess() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        authDao.clear();
        AuthData foundAuth = authDao.getAuth(authToken);

        assertNull(foundAuth);
    }

    @Test
    public void storeAuthSuccess() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        AuthData foundAuth = authDao.getAuth(authToken);
        assertNotNull(foundAuth);
        assertEquals(authToken, foundAuth.authToken());
        assertEquals("Max", foundAuth.username());
    }

    @Test
    public void storeDuplicatedAuth() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        AuthData repeatedAuth = new AuthData(authToken, "Max");

        Assertions.assertThrows(DataAccessException.class, () -> authDao.storeAuth(repeatedAuth));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        AuthData foundAuth = authDao.getAuth(authToken);
        assertNotNull(foundAuth);
        assertEquals("Max", foundAuth.username());

        assertEquals(authToken, foundAuth.authToken());
    }

    @Test
    public void getAuthNotFound() throws DataAccessException {
        AuthData foundAuth = authDao.getAuth("dummyToken");
        assertNull(foundAuth);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        authDao.deleteAuth(auth);

        AuthData foundAuth = authDao.getAuth(authToken);
        assertNull(foundAuth);
    }

    @Test
    public void deleteNonexistentAuth() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, "Max");
        authDao.storeAuth(auth);

        // try to delete a fake authentication
        AuthData fakeAuth = new AuthData(UUID.randomUUID().toString(), "John");
        authDao.deleteAuth(fakeAuth);

        assertNotNull(authDao.getAuth(authToken));
    }

}
