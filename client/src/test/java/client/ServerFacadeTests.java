package client;

import model.GameData;
import org.junit.jupiter.api.*;
import requests.*;
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
    static void cleanup() throws Exception {
        new ServerFacade(url).clear();
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

    @Test
    public void logoutSuccess() throws ResponseException {
        // register a user first
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);
        // login
        LoginRequest loginRequest = new LoginRequest("Max", "password");
        LoginResult loginResult = facade.login(loginRequest);
        // now logout
        LogoutResult logoutResult = facade.logout();

        Assertions.assertNull(facade.getAuthToken());
    }

    @Test
    public void logoutFailure() throws ResponseException {
        Assertions.assertThrows(ResponseException.class, () -> facade.logout());
    }

    @Test
    public void clearSuccess() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Pro Game");
        CreateResult createResult = facade.createGame(createRequest);

        facade.clear();

        // need to register another user in order to have a valid authToken to list the games
        RegisterRequest registerRequest2 = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest2);

        Assertions.assertTrue(facade.listGames().games().isEmpty());
    }

    // clear Fail doesn't make sense to build

    @Test
    public void listGamesSuccess() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        CreateRequest request1 = new CreateRequest("Pro Game");
        facade.createGame(request1);
        CreateRequest request2 = new CreateRequest("Casual Game");
        facade.createGame(request2);
        CreateRequest request3 = new CreateRequest("Beginner Game");
        facade.createGame(request3);

        ListResult result = facade.listGames();
        Assertions.assertEquals(3, result.games().size());
    }

    @Test
    // try to list games without having an authToken
    public void listGamesFailure() throws ResponseException {
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
    }

    @Test
    public void createGameSuccess() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        CreateRequest request = new CreateRequest("Pro Game");
        CreateResult result = facade.createGame(request);
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    // try to create a game without an authToken
    public void createGameFailure() throws ResponseException {
        CreateRequest request = new CreateRequest(null);
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame(request));
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        // create a game
        CreateRequest createRequest = new CreateRequest("Pro Game");
        CreateResult createResult = facade.createGame(createRequest);
        //  join a game
        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.gameID());
        JoinResult joinResult = facade.joinGame(joinRequest);
        // list the games and make sure the white slot is taken with the proper username
        ListResult result = facade.listGames();

        GameData game = result.games().iterator().next();
        Assertions.assertEquals("Max", game.whiteUsername());
    }

    @Test
    // negative test for joinGame
    // try to join the game when a color is already taken
    public void joinGameFailure() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("Max", "password", "max@gmail.com");
        facade.register(registerRequest);

        // create a game
        CreateRequest createRequest = new CreateRequest("Pro Game");
        CreateResult createResult = facade.createGame(createRequest);

        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.gameID());
        // join the game as white
        facade.joinGame(joinRequest);
        // try to join the game as white again and fail
        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(joinRequest));
    }

}
