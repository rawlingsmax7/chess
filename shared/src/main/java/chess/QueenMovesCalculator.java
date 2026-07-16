package chess;

import java.util.Collection;

public class QueenMovesCalculator extends LineMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] posDirections = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        return lineMove(board, position, posDirections);
    }
}
