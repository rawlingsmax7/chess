package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // we are mapping gameIDs to sockets of everyone who is participating in game (players and
    // observers)
    // need concurrent because two players could connect at the same time
    private final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();

    Gson gson = new Gson();

    public void add(Integer gameID, Session session) {
        // we want to get the set associated with the gameID and then work with that
        Set<Session> sessions = connections.get(gameID);
        // if no one is in the game yet we should make a new set
        if (sessions == null) {
            sessions = ConcurrentHashMap.newKeySet();
            connections.put(gameID, sessions);
        }
        sessions.add(session);
    }

    public void remove(Integer gameID, Session session) {
        Set<Session> sessions = connections.get(gameID);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
    }

    // notify all other clients except for the sessionToExclude
    public void broadcast(Integer gameID, Session sessionToExclude, ServerMessage serverMessage) throws IOException {
        String msg = gson.toJson(serverMessage);
        // loop through all the sessions based on their gameID
        if (connections.get(gameID) == null) {
            return;
        }
        for (Session c : connections.get(gameID)) {
            if (c.isOpen()) {
                if (!c.equals(sessionToExclude)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }

}
