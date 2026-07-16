package chess;

import java.util.Collection;

public class RookMovesCalculator extends LineMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // right, up, down, left
        int[][] directions = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}};

        return lineMove(board, position, directions);
    }
}
