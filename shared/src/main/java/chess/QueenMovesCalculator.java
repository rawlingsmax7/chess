package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> posMoves = new ArrayList<ChessMove>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[][] posDirections = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        ChessPiece actingPiece = board.getPiece(position);

        for (int i = 0; i < posDirections.length; i++) {
            int rowStep = posDirections[i][0];
            int colStep = posDirections[i][1];
            for (int row = currentRow + rowStep,
                 col = currentCol + colStep;
                 (1 <= row && row <= 8) && (1 <= col && col <= 8);
                 row += rowStep, col += colStep) {

                ChessPosition posPosition = new ChessPosition(row, col);
                // need to check if piece is at position where we might move
                ChessPiece pieceInWay = board.getPiece(posPosition);

                if (pieceInWay == null) {
                    // empty spot
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    posMoves.add(posMove);

                } else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                    // same team
                    break;
                } else {
                    // enemy team
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    posMoves.add(posMove);
                    break;
                }
            }
        }

        return posMoves;
    }
}
