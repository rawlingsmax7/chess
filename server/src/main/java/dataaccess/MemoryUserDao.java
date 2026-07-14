package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDao implements UserDao {
    // maps usernames to their UserData
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void storeUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
