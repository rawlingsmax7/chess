package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // this is the array list that we want to add all the moves to
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        // check upper right diagonal possible spots
        for (int row = currentRow + 1, col = currentCol + 1; (row <= board.squares.length) && (col <= board.squares.length); row++, col++) {
            ChessPosition possiblePosition = new ChessPosition(row, col);
            ChessMove possibleMove = new ChessMove(position, possiblePosition, null);
            possibleMoves.add(possibleMove);
        }

        // check upper left diagonal
        for (int row = currentRow + 1, col = currentCol - 1; (row <= board.squares.length) &&  (col >= 1); row++, col--) {
            ChessPosition possiblePos = new ChessPosition(row, col);
            ChessMove posMove = new ChessMove(position, possiblePos, null);
            possibleMoves.add(posMove);
        }

        // check bottom left diagonal
        for (int row = currentRow - 1, col = currentCol-1; (row >= 1) && (col >= 1); row--, col--) {
            ChessPosition posPosition = new ChessPosition(row, col);
            ChessMove posMove = new ChessMove(position, posPosition, null);
            possibleMoves.add(posMove);
        }

        // check bottom right diagonal
        for (int row = currentRow - 1, col = currentCol+1; (row >= 1) && (col <= board.squares.length); row--, col++) {
            ChessPosition posPosition = new ChessPosition(row, col);
            ChessMove posMove = new ChessMove(position, posPosition, null);
            possibleMoves.add(posMove);
        }

        return possibleMoves;
    }
}
