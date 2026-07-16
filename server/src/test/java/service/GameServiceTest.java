package service;

import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;

public class GameServiceTest {
    private GameService gameService;
    private UserService userService;
    private String authToken;

    @BeforeEach
    public void setupTest() throws DataAccessException {
        UserDao userDao = new MemoryUserDao();
        AuthTokenDao authDao = new MemoryAuthTokenDao();
        GameDao gameDao = new MemoryGameDao();
        gameService = new GameService(gameDao, authDao);
        userService = new UserService(userDao, authDao);

        // register a user to have an authToken to pass to methods
        RegisterRequest request = new RegisterRequest("Max", "password", "max@gmail.com");
        RegisterResult result = userService.register(request);
        authToken = result.authToken();
    }

    @Test
    // positive test for createGame
    public void createGameSuccess() throws DataAccessException {
        CreateRequest request = new CreateRequest("Pro Game");
        CreateResult result = gameService.createGame(authToken, request);
        Assertions.assertEquals(1, result.gameID());
    }

    @Test
    // negative test for createGame
    // trying to create a Game with no gameName is a bad request
    public void createGameFailure() throws DataAccessException {
        CreateRequest request = new CreateRequest(null);
        Assertions.assertThrows(BadRequestException.class, () -> gameService.createGame(authToken, request));
    }

    @Test
    // positive test for listGames
    public void listGamesSuccess() throws DataAccessException {
        // create some games
        CreateRequest request1 = new CreateRequest("Pro Game");
        gameService.createGame(authToken, request1);
        CreateRequest request2 = new CreateRequest("Casual Game");
        gameService.createGame(authToken, request2);
        CreateRequest request3 = new CreateRequest("Beginner Game");
        gameService.createGame(authToken, request3);

        ListResult result = gameService.listGames(authToken);
        Assertions.assertEquals(3, result.games().size());
    }

    @Test
    // negative test for listGames
    // pass a bad token and get unauthorized exception thrown
    public void listGamesFailure() throws DataAccessException {
        Assertions.assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad token"));
    }

    @Test
    // positive test for joinGame
    public void joinGameSuccess() throws DataAccessException {
        // create a game
        CreateRequest createRequest = new CreateRequest("Pro Game");
        CreateResult createResult = gameService.createGame(authToken, createRequest);
        //  join a game
        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.gameID());
        JoinResult joinResult = gameService.joinGame(authToken, joinRequest);
        // list the games and make sure the white slot is taken with the proper username
        ListResult result = gameService.listGames(authToken);

        GameData game = result.games().iterator().next();
        Assertions.assertEquals("Max", game.whiteUsername());
    }

    @Test
    // negative test for joinGame
    // try to join the game when a color is already taken
    public void joinGameFailure() throws DataAccessException {
        // create a game
        CreateRequest createRequest = new CreateRequest("Pro Game");
        CreateResult createResult = gameService.createGame(authToken, createRequest);

        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.gameID());
        // join the game as white
        gameService.joinGame(authToken, joinRequest);
        // try to join the game as white again and fail
        Assertions.assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(authToken, joinRequest));
    }
}
