package chess;

import java.util.Collection;

public class KingMovesCalculator extends StepMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        return stepMove(board, position, directions);
    }
}
