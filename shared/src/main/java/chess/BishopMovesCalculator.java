package chess;

import java.util.Collection;

public class BishopMovesCalculator extends LineMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // array list to store different diagonal positions
        // first number is row, second is column
        // up right, then up left, then down left, then down right
        int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        return lineMove(board, position, directions);
    }
}
