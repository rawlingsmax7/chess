package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import requests.CreateRequest;
import requests.CreateResult;


public class GameService {
    private final GameDao gameDao;
    private final AuthTokenDao authDao;
    private int nextGameID = 1;

    public GameService(GameDao gameDao, AuthTokenDao authDao) {
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public CreateResult createGame(String authToken, CreateRequest request) throws DataAccessException {
        String gameName = request.gameName();

        AuthData authData = authDao.getAuth(authToken);
        // make sure that the authentication token used to create a game exists in the database
        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // make sure the data in the request is good
        if (gameName == null) {
            throw new BadRequestException("Error: bad request");
        }

        int newID = nextGameID;
        nextGameID++; // increment the nextGameID

        GameData game = new GameData(newID, null, null, gameName, new ChessGame());
        gameDao.storeGame(game);
        return new CreateResult(newID);
    }
}
