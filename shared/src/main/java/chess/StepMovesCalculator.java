package chess;

import java.util.ArrayList;
import java.util.Collection;

abstract class StepMovesCalculator implements ChessMovesCalculator {
    protected Collection<ChessMove> stepMove(ChessBoard board, ChessPosition position, int[][] stepDirections) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece actingPiece = board.getPiece(position);

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        for (int[] stepDirection : stepDirections) {
            int rowStep = stepDirection[0];
            int colStep = stepDirection[1];

            ChessPosition endingPosition = new ChessPosition(currentRow + rowStep, currentCol + colStep);
            if (!board.isInBounds(endingPosition.getRow(), endingPosition.getColumn())) {
                continue;
            }

            // empty spot or enemy team valid
            // if it's the same team we skip it
            ChessPiece pieceInWay = board.getPiece(endingPosition);
            if (pieceInWay == null || pieceInWay.getTeamColor() != actingPiece.getTeamColor()) {
                possibleMoves.add(new ChessMove(position, endingPosition, null));
            }
        }
        return possibleMoves;
    }
}
