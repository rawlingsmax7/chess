package dataaccess;

import model.UserData;

public interface UserDao {
    void clear();

    void storeUser(UserData user);

    UserData getUser(String username);

}
