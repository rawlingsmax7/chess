package dataaccess;

import model.AuthData;

public interface AuthTokenDao {
    void clear();

    void storeAuth(AuthData auth);

    AuthData getAuth(String authToken);

    void deleteAuth(AuthData auth);

}
