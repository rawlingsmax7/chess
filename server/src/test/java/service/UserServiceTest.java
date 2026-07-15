package service;

import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setupTest() {
        UserDao userDao = new MemoryUserDao();
        AuthTokenDao authDao = new MemoryAuthTokenDao();
        userService = new UserService(userDao, authDao);
    }

    // positive test for register
    @Test
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Max", "password", "max@gmail.com");
        RegisterResult result = userService.register(request);
        Assertions.assertEquals("Max", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    // negative test for register
    // we try to register a username that's already been taken
    @Test
    public void registerFailure() throws DataAccessException {
        // first register succeeds
        RegisterRequest initialRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        userService.register(initialRequest);

        RegisterRequest nextRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        Assertions.assertThrows(AlreadyTakenException.class, () -> userService.register(nextRequest));
    }

    // positive test for login
    @Test
    public void loginSuccess() throws DataAccessException {
        // register a user first
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        userService.register(registerRequest);

        // create and perform the login request
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        LoginResult result = userService.login(loginRequest);

        Assertions.assertEquals("Max", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    // negative test for login
    // we try to login with a username that doesn't exist yet
    @Test
    public void loginFailure() throws DataAccessException {
        // create and perform the login request
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        Assertions.assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    // positive test for logout
    @Test
    public void logoutSuccess() throws DataAccessException {
        // register a user first
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        userService.register(registerRequest);
        // login user
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        LoginResult loginResult = userService.login(loginRequest);
        // logout user
        LogoutRequest logoutRequest = new LogoutRequest(loginResult.authToken());
        userService.logout(logoutRequest);

        // logging out the user deletes the authData so logging out again should fail
        Assertions.assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }

    // negative test for logout
    // logout with an invalid token
    @Test
    public void logoutFailure() throws DataAccessException {
        LogoutRequest logoutRequest = new LogoutRequest("fake token");
        Assertions.assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }
}
