package service;

import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.RegisterRequest;
import requests.RegisterResult;

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
}
