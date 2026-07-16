package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator implements ChessMovesCalculator {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        // right, up, down, left
        int[][] directions = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}};

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        ChessPiece actingPiece = board.getPiece(position);

        for (int i = 0; i < directions.length; i++) {
            int rowStep = directions[i][0];
            int colStep = directions[i][1];

            for (int row = currentRow + rowStep,
                 col = currentCol + colStep;
                 (1 <= row && row <= 8) && (1 <= col && col <= 8);
                 row += rowStep, col += colStep) {
                ChessPosition posPosition = new ChessPosition(row, col);

                // check if pieces in way
                ChessPiece pieceInWay = board.getPiece(posPosition);
                if (pieceInWay == null) {
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    possibleMoves.add(posMove);
                } else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                    // friendly piece
                    break;
                } else {
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    possibleMoves.add(posMove);
                    break;
                }
            }
        }

        return possibleMoves;
    }
}
