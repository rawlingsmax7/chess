package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.*;

import java.util.UUID;

public class UserService {
    private final UserDao userDao;
    private final AuthTokenDao authDao;

    public UserService(UserDao userDao, AuthTokenDao authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        // make sure the data in the request is good
        if (username == null || password == null || email == null) {
            throw new BadRequestException("Error: bad request");
        }
        // if we find that that username is already in the database then throw an AlreadyTakenException
        if (userDao.getUser(username) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }
        UserData user = new UserData(username, password, email);
        userDao.storeUser(user);

        AuthData auth = new AuthData(generateAuthToken(), username);
        authDao.storeAuth(auth);

        return new RegisterResult(username, auth.authToken());
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        String username = request.username();
        String password = request.password();
        // make sure the data in the request is good
        if (username == null || password == null) {
            throw new BadRequestException("Error: bad request");
        }

        UserData user = userDao.getUser(username);
        // if that username hasn't been registered or if the password doesn't match what's stored with that user throw exception
        if (user == null || !BCrypt.checkpw(password, user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        AuthData auth = new AuthData(generateAuthToken(), username);
        authDao.storeAuth(auth);

        return new LoginResult(username, auth.authToken());
    }

    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        String authToken = request.authToken();
        // make sure the data in the request is good
        if (authToken == null) {
            throw new BadRequestException("Error: bad request");
        }

        AuthData authData = authDao.getAuth(authToken);
        // make sure that the authentication data requested to delete actually exists; if not throw an exception
        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        authDao.deleteAuth(authData);

        return new LogoutResult();
    }
}
