package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDao {
    void clear() throws DataAccessException;

    void storeGame(GameData game) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    Collection<GameData> getGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;
}
