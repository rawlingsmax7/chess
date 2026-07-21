package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import requests.*;


public class GameService {
    private final GameDao gameDao;
    private final AuthTokenDao authDao;

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
        // the gameID 0 actually gets overwritten by the GameDao
        GameData game = new GameData(0, null, null, gameName, new ChessGame());
        int newID = gameDao.storeGame(game);
        return new CreateResult(newID);
    }

    public ListResult listGames(String authToken) throws DataAccessException {
        AuthData authData = authDao.getAuth(authToken);
        // make sure that the authentication token used to list games exists in the database
        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        return new ListResult(gameDao.getGames());
    }

    public JoinResult joinGame(String authToken, JoinRequest request) throws DataAccessException {
        int gameID = request.gameID();
        String requestedPlayerColor = request.playerColor();

        AuthData authData = authDao.getAuth(authToken);

        // make sure that the authentication token used to join a game exists in the database
        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // make sure the data in the request is good
        // if the requested color doesn't equal black or white then throw error
        if (!"WHITE".equals(requestedPlayerColor) && !"BLACK".equals(requestedPlayerColor)) {
            throw new BadRequestException("Error: bad request");
        }

        GameData game = gameDao.getGame(gameID);
        // if the gameID found no actual game then it was a bad request
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (requestedPlayerColor.equals("WHITE") && whiteUsername != null) {
            throw new AlreadyTakenException("Error: already taken");
        }
        if (requestedPlayerColor.equals("BLACK") && blackUsername != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        if (requestedPlayerColor.equals("WHITE")) {
            GameData updatedGameData = new GameData(gameID, authData.username(), blackUsername, game.gameName(), game.game());
            gameDao.updateGame(updatedGameData);
        }
        // else the requested color is black
        else {
            GameData updatedGameData = new GameData(gameID, whiteUsername, authData.username(), game.gameName(), game.game());
            gameDao.updateGame(updatedGameData);
        }
        return new JoinResult();
    }
}
