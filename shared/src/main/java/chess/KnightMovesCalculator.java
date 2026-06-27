package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator  implements ChessMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> posMoves = new ArrayList<ChessMove>();

        int[][] posDirections = {{1,2},{-1,2},{2,1},{2,-1},{1,-2},{-1,-2},{-2,1},{-2,-1}};

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        ChessPiece actingPiece = board.getPiece(position);

        for (int i = 0; i < posDirections.length; i++) {
            int rowStep = posDirections[i][0];
            int colStep = posDirections[i][1];

            ChessPosition posPosition = new ChessPosition(currentRow + rowStep, currentCol + colStep);

            int row = posPosition.getRow();
            int col = posPosition.getColumn();

            if ((1 <= row && row <= 8) && (1 <= col && col <= 8)) {

                ChessPiece pieceInWay = board.getPiece(posPosition);

                if (pieceInWay == null) {
                    // empty spot
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    posMoves.add(posMove);

                }
                else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                    // same team
                }
                else {
                    // enemy team
                    ChessMove posMove = new ChessMove(position, posPosition, null);
                    posMoves.add(posMove);
                }
            }
        }

        return posMoves;
    }
}
