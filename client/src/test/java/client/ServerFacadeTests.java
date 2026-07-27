package client;

import org.junit.jupiter.api.*;
import requests.LoginRequest;
import requests.LoginResult;
import requests.RegisterRequest;
import requests.RegisterResult;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static String url;
    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        url = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void setupTest() throws ResponseException {
        facade = new ServerFacade(url);
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("Max", "password", "max@gmail.com");
        RegisterResult result = facade.register(request);
        Assertions.assertEquals("Max", result.username());
        Assertions.assertNotNull(facade.getAuthToken());
    }

    @Test
    public void registerDuplicate() throws ResponseException {
        // first register succeeds
        RegisterRequest initialRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(initialRequest);

        RegisterRequest nextRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        Assertions.assertThrows(ResponseException.class, () -> facade.register(nextRequest));
    }

    @Test
    public void loginSuccess() throws ResponseException {
        // register a user first
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        // create and perform the login request
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        LoginResult result = facade.login(loginRequest);

        Assertions.assertEquals("Max", result.username());
    }

    @Test
    public void loginUsernameNonexistent() throws ResponseException {
        // create and perform the login request
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        Assertions.assertThrows(ResponseException.class, () -> facade.login(loginRequest));
    }


}
