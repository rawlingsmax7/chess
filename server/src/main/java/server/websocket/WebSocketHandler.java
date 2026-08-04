package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthTokenDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.UnauthorizedException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
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
            UserGameCommand command = gson.fromJson(
                    wsMessageContext.message(), UserGameCommand.class);
            int gameID = command.getGameID();
            String username = getUsername(command.getAuthToken());
            connections.add(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
//                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
//                case LEAVE -> leaveGame(session, username, command);
//                case RESIGN -> resign(session, username, command);
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
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
        String role = "";
        if (Objects.equals(username, gameData.whiteUsername())) {
            role = "white";
        } else if (Objects.equals(username, gameData.blackUsername())) {
            role = "black";
        } else {
            role = "observer";
        }

        String broadcastMessage = username + " connected to game as " + role;
        connections.broadcast(gameID, session, new NotificationMessage(broadcastMessage));
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


    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

}
