package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlGameDaoTest {

    private MySqlGameDao gameDao;

    @BeforeEach
    public void setupTest() throws DataAccessException {
        gameDao = new MySqlGameDao();
        gameDao.clear();
    }

    // positive clear test
    @Test
    public void clearSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Pro Game", new ChessGame());
        int id = gameDao.storeGame(game);

        gameDao.clear();
        // assert that the list of games is empty
        Assertions.assertTrue(gameDao.getGames().isEmpty());
    }

    @Test
    public void storeGameSuccess() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        // the 0 is actually throwaway
        GameData game = new GameData(0, null, null, "Pro Game", newGame);
        int id = gameDao.storeGame(game);

        GameData foundGame = gameDao.getGame(id);
        assertNotNull(foundGame);
        assertEquals(id, foundGame.gameID());
        assertNull(foundGame.whiteUsername());
        assertNull(foundGame.blackUsername());
        assertEquals("Pro Game", foundGame.gameName());
        assertEquals(newGame, foundGame.game());
    }

    @Test
    public void storeGameNullName() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        // the 0 is actually throwaway
        GameData game = new GameData(0, null, null, null, newGame);
        // because the name is null this should throw an error
        Assertions.assertThrows(DataAccessException.class, () -> gameDao.storeGame(game));
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        // the 0 is actually throwaway
        GameData game = new GameData(0, null, null, "Pro Game", newGame);
        int id = gameDao.storeGame(game);

        GameData foundGame = gameDao.getGame(id);
        assertEquals(id, foundGame.gameID());
        assertNull(foundGame.whiteUsername());
        assertNull(foundGame.blackUsername());
        assertEquals("Pro Game", foundGame.gameName());
        assertEquals(newGame, foundGame.game());
    }

    @Test
    public void getGameNotFound() throws DataAccessException {
        Assertions.assertNull(gameDao.getGame(90));
    }

    @Test
    public void getGamesSuccess() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        // the 0 is actually throwaway
        GameData game = new GameData(0, null, null, "Pro Game", newGame);
        int id = gameDao.storeGame(game);
        ChessGame newGame2 = new ChessGame();
        // the 0 is actually throwaway
        GameData game2 = new GameData(0, null, null, "Amatuer Game", newGame2);
        int id2 = gameDao.storeGame(game2);

        Collection<GameData> gameList = gameDao.getGames();
        Assertions.assertEquals(2, gameList.size());
    }

    @Test
    public void getGamesEmpty() throws DataAccessException {
        Collection<GameData> gameList = gameDao.getGames();
        Assertions.assertTrue(gameList.isEmpty());
    }

    @Test
    public void updateGameSuccess() throws DataAccessException, InvalidMoveException {
        ChessGame newGame = new ChessGame();
        // the 0 is actually throwaway
        GameData game = new GameData(0, null, null, "Pro Game", newGame);
        int id = gameDao.storeGame(game);

        GameData foundGame = gameDao.getGame(id);
        ChessGame gameToEdit = foundGame.game();

        // move the white pawn
        ChessPosition startingPosition = new ChessPosition(2, 5);
        ChessPosition endingPosition = new ChessPosition(4, 5);

        ChessMove firstMove = new ChessMove(startingPosition, endingPosition, null);

        gameToEdit.makeMove(firstMove);

        GameData updatedGame = new GameData(id, "Max", null, "Pro Game", gameToEdit);

        gameDao.updateGame(updatedGame);

        GameData whitePawnMovedGame = gameDao.getGame(id);
        assertEquals("Max", whitePawnMovedGame.whiteUsername());
        assertNull(whitePawnMovedGame.game().getBoard().getPiece(new ChessPosition(2, 5)));
        assertNotNull(whitePawnMovedGame.game().getBoard().getPiece(new ChessPosition(4, 5)));
    }

    @Test
    public void updateNonexistentGame() throws DataAccessException {
        GameData game = new GameData(90, null, null, "Fake Game", new ChessGame());
        gameDao.updateGame(game);

        assertNull(gameDao.getGame(90));
    }

}
