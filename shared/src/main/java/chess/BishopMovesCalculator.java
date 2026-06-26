package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // this is the array list that we want to add all the moves to
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        // fetching an existing piece so we don't need new keyword here
        ChessPiece actingPiece = board.getPiece(position);

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

        // check bottom right diagonal
        for (int row = currentRow - 1, col = currentCol+1; (row >= 1) && (col <= board.squares.length); row--, col++) {
            ChessPosition posPosition = new ChessPosition(row, col);
            ChessMove posMove = new ChessMove(position, posPosition, null);
            possibleMoves.add(posMove);
        }

        return possibleMoves;
    }
}
