package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDao {
    void clear();

    void storeGame(GameData game);

    GameData getGame(Integer gameID);

    Collection<GameData> getGames();

    void updateGame(GameData game);
}
