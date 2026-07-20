package dataaccess;

import model.AuthData;

public interface AuthTokenDao {
    void clear() throws DataAccessException;

    void storeAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;

}
