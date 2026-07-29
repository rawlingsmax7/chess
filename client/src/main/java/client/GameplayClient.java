package client;

import chess.ChessBoard;
import model.GameData;
import ui.DrawBoard;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class GameplayClient {

    private final ServerFacade facade;
    private final boolean whitePerspective;
    private final GameData game;

    GameplayClient(ServerFacade facade, boolean whitePerspective, GameData game) {
        this.facade = facade;
        this.whitePerspective = whitePerspective;
        this.game = game;
    }

    public void run() {
        PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard board = game.game().getBoard();
        new DrawBoard(board, output).draw(whitePerspective);
    }

}
