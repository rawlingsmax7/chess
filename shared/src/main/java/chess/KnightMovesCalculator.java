package chess;

import java.util.Collection;

public class KnightMovesCalculator extends StepMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{1, 2}, {-1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, 1}, {-2, -1}};

        return stepMove(board, position, directions);
    }
}
