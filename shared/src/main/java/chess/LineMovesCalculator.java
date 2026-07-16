package chess;

import java.util.ArrayList;
import java.util.Collection;

abstract class LineMovesCalculator implements ChessMovesCalculator {
    protected Collection<ChessMove> lineMove(ChessBoard board, ChessPosition position,
                                             int[][] slideDirections) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ChessPiece actingPiece = board.getPiece(position);

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        for (int[] slideDirection : slideDirections) {
            int rowStep = slideDirection[0];
            int colStep = slideDirection[1];

            for (int row = currentRow + rowStep,
                 col = currentCol + colStep;
                 board.isInBounds(row, col);
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
                // if it's enemy we add that as a possible move and break can it's blocking the way
                // for the rest has to be an enemy logically
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
