package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // this is the array list that we want to add all the moves to
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        // array list to store different diagonal positions
        // first number is row, second is column
        // up right, then up left, then down left, then down right
        int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        // fetching an existing piece so we don't need new keyword here
        ChessPiece actingPiece = board.getPiece(position);

        // iterate through each of the possible directions
        for (int i = 0; i < directions.length; i++) {
            int rowStep = directions[i][0];
            int colStep = directions[i][1];

            for (int row = currentRow + rowStep,
                 col = currentCol + colStep;
                 (1 <= row && row <= board.squares.length) &&
                         (1 <= col && col <= board.squares.length);
                 row += rowStep, col += colStep) {
                ChessPosition posPosition = new ChessPosition(row, col);
                // we want to check if this position is a fellow teammate
                ChessPiece pieceInWay = board.getPiece(posPosition);
                // if it's null then we just add the position and can keep checking
                if (pieceInWay == null) {
                    ChessMove posMove = new ChessMove(position, posPosition, null);

                    possibleMoves.add(posMove);
                }
                // it's friendly it's not valid, we break
                else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                    break;
                }
                // if it's enemy we add that as a possible move and break can it's blocking the way for the rest
                // has to be an enemy logically
                else {
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    possibleMoves.add(posMove);
                    break;
                }
            }
        }

        return possibleMoves;
    }
}
