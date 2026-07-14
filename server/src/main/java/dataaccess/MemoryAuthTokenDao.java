package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthTokenDao implements AuthTokenDao {
    // maps token strings keys to AuthData values
    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() {
        auths.clear();
    }

    @Override
    public void storeAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(AuthData auth) {
        auths.remove(auth.authToken());
    }
}
