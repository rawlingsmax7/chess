package websocket;

import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// opnes the socket
public class WebSocketFacade extends Endpoint {

    private Session session;
    private ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketFacade(String serverURL, ServerMessageObserver observer) throws URISyntaxException, DeploymentException, IOException {
        this.observer = observer;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // /ws endpoint
        String websocketURL = serverURL.replace("http", "ws") + "/ws";
        URI uri = new URI(websocketURL);
        this.session = container.connectToServer(this, uri);

        // tell where messages should go
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                // need to parse twice to figure out which message type it is
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                switch (serverMessage.getServerMessageType()) {
                    case NOTIFICATION ->
                            observer.notify(gson.fromJson(message, NotificationMessage.class));
                    case ERROR -> observer.notify(gson.fromJson(message, ErrorMessage.class));

                    case LOAD_GAME ->
                            observer.notify(gson.fromJson(message, LoadGameMessage.class));

                }
            }
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void send(UserGameCommand command) throws IOException {
        this.session.getBasicRemote().sendText(gson.toJson(command));
    }
}
