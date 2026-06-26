package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        // check upper diagonal possible spots
        for (int row = currentRow + 1, col = currentCol + 1; (row <= board.squares.length) && (col <= board.squares.length); row++, col++) {
            ChessPosition possiblePosition = new ChessPosition(row, col);
            ChessMove possibleMove = new ChessMove(position, possiblePosition, null);
            possibleMoves.add(possibleMove);
        }

        return possibleMoves;
    }
}
