package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthTokenDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.UnauthorizedException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

// this class helps accept the web socket requests
public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final Gson gson = new Gson();

    private final ConnectionManager connections = new ConnectionManager();
    private final GameDao gameDao;
    private final AuthTokenDao authDao;

    public WebSocketHandler(GameDao gameDao, AuthTokenDao authDao) {
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext wsMessageContext) throws Exception {
        Session session = wsMessageContext.session;
        try {
            UserGameCommand command = gson.fromJson(wsMessageContext.message(),
                    UserGameCommand.class);
            int gameID = command.getGameID();
            String username = getUsername(command.getAuthToken());
            connections.add(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                // need to deserialize twice because the makeMove command contains more info
                case MAKE_MOVE -> makeMove(session, username,
                        gson.fromJson(wsMessageContext.message(), MakeMoveCommand.class));
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private void resign(Session session, String username, UserGameCommand command) throws DataAccessException, IOException {
        int gameID = command.getGameID();
        GameData gameData = gameDao.getGame(gameID);

        if (gameData == null) {
            sendMessage(session, new ErrorMessage("Error: game ID bad"));
            return;
        }
        ChessGame.TeamColor role = getRole(username, gameData);
        ChessGame chessGame = gameData.game();

        // if it's an observer then you can't resign, need to be one of the players
        if (role == null) {
            sendMessage(session, new ErrorMessage("Error: observers can't resign"));
            return;
        }
        // game is already over you cant resign
        if (chessGame.isGameOver()) {
            sendMessage(session, new ErrorMessage("Error: Game is already over"));
            return;
        }

        chessGame.setGameOver();

        // update game in database after setting it to game over
        gameDao.updateGame(gameData);

        // Server sends a Notification message to all clients in that game informing them
        // that the root client resigned. This applies to both players and observers.
        String broadcastMessage = username + " resigned as " + role;
        connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
    }

    private void leaveGame(Session session, String username, UserGameCommand command) throws DataAccessException, IOException {
        int gameID = command.getGameID();
        GameData gameData = gameDao.getGame(gameID);

        if (gameData == null) {
            sendMessage(session, new ErrorMessage("Error: game ID bad"));
            return;
        }
        ChessGame.TeamColor role = getRole(username, gameData);


        // leaving game as white so set whiteusername to null
        if (role == ChessGame.TeamColor.WHITE) {
            GameData updatedGame = new GameData(gameID, null, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
            gameDao.updateGame(updatedGame);
        } else if (role == ChessGame.TeamColor.BLACK) {
            GameData updatedGame = new GameData(gameID, gameData.whiteUsername(), null,
                    gameData.gameName(), gameData.game());
            gameDao.updateGame(updatedGame);
        }

        // Server sends a Notification message to all other clients in that game informing them
        // that the root client left. This applies to both players and observers.
        String broadcastMessage = "";
        if (role == null) {
            broadcastMessage = username + " left game as an observer";
        } else {
            broadcastMessage = username + " left game as " + role;
        }
        connections.broadcast(gameID, session, new NotificationMessage(broadcastMessage));

        connections.remove(gameID, session);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws DataAccessException, IOException {
        int gameID = command.getGameID();
        GameData gameData = gameDao.getGame(gameID);

        if (gameData == null) {
            sendMessage(session, new ErrorMessage("Error: game ID bad"));
            return;
        }
        ChessGame.TeamColor role = getRole(username, gameData);

        ChessGame chessGame = gameData.game();
        // game is over so error
        if (chessGame.isGameOver()) {
            sendMessage(session, new ErrorMessage("Error: game is over"));
            return;
        }

        // if it's an observer then it's an error
        if (role == null) {
            sendMessage(session, new ErrorMessage("Error: observers can't make moves"));
            return;
        }

        // if a player tries to play when it's not their turn it's an error
        if (role != chessGame.getTeamTurn()) {
            sendMessage(session, new ErrorMessage("Error: not your turn"));
            return;
        }

        try {
            chessGame.makeMove(command.getMove());
        } catch (InvalidMoveException exception) {
            sendMessage(session, new ErrorMessage("Error: not a valid move"));
            return;
        }

        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                chessGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            chessGame.setGameOver();
        }

        // update the game in the database
        gameDao.updateGame(gameData);

        // Server sends a LOAD_GAME message to all clients in the game (including the root client) with an updated game.
        connections.broadcast(gameID, null, new LoadGameMessage(chessGame));

        // Server sends a Notification message to all other clients in that game informing them
        // what move was made
        ChessMove move = command.getMove();
        ChessPosition startingPosition = move.getStartPosition();
        ChessPosition endingPosition = move.getEndPosition();
        String moveBroadcastMessage = String.format("%s moved %s to %s", username,
                positionToSquare(startingPosition), positionToSquare(endingPosition));
        connections.broadcast(gameID, session, new NotificationMessage(moveBroadcastMessage));

        // If the move results in check, checkmate or stalemate the server sends a Notification
        // message to all clients.
        // Checkmate, then Stalemate, then Check
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();

        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            String broadcastMessage = String.format("%s is in Checkmate. %s wins.", whiteUsername, blackUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        } else if (chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            String broadcastMessage = String.format("%s is in Checkmate. %s wins.", blackUsername, whiteUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        } else if (chessGame.isInStalemate(ChessGame.TeamColor.WHITE)) {
            String broadcastMessage = String.format("%s is in Stalemate. Tie game.", whiteUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        } else if (chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            String broadcastMessage = String.format("%s is in Stalemate. Tie game.", blackUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        } else if (chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            String broadcastMessage = String.format("%s is in Check.", whiteUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        } else if (chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            String broadcastMessage = String.format("%s is in Check.", blackUsername);
            connections.broadcast(gameID, null, new NotificationMessage(broadcastMessage));
        }
    }

    private void connect(Session session, String username, UserGameCommand command) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        GameData gameData = gameDao.getGame(gameID);
        if (gameData == null) {
            sendMessage(session, new ErrorMessage("Error: game ID bad"));
            return;
        }
        // otherwise success
        // send a LOAD_GAME message back to the root client.
        sendMessage(session, new LoadGameMessage(gameData.game()));
        // send a Notification message to all other clients in game
        // informing them the root client connected to the game, either as a
        // player (in which case their color must be specified) or as an observer
        ChessGame.TeamColor role = getRole(username, gameData);

        // if the role is an observer just put that in
        String broadcastMessage = "";
        if (role == null) {
            broadcastMessage = username + " connected to game as observer";
        } else {
            broadcastMessage = username + " connected to game as " + role;
        }
        connections.broadcast(gameID, session, new NotificationMessage(broadcastMessage));
    }

    private ChessGame.TeamColor getRole(String username, GameData gameData) {
        ChessGame.TeamColor role = null;
        if (Objects.equals(username, gameData.whiteUsername())) {
            role = ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(username, gameData.blackUsername())) {
            role = ChessGame.TeamColor.BLACK;
        }
        return role;
    }

    private String getUsername(String authToken) throws DataAccessException {
        AuthData auth = authDao.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth.username();
    }

    private void sendMessage(Session session, ServerMessage message) throws IOException {
        session.getRemote().sendString(gson.toJson(message));
    }

    String positionToSquare(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        int rank = position.getRow();
        return String.format("%c%d", file, rank);
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

}
