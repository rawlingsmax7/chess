package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDao implements GameDao {
    // maps gameIDs to GameData
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public void storeGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(Integer gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> getGames() {
        // .values() gives a collection of values and not just a map
        return games.values();
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }
}
