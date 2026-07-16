package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessPiece.PieceType.*;

public class PawnMovesCalculator implements ChessMovesCalculator {

    static final ChessPiece.PieceType[] PROMOTION_PIECES = {QUEEN, BISHOP, KNIGHT, ROOK};

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece actingPiece = board.getPiece(position);
        int startingRow = position.getRow();
        int startingCol = position.getColumn();

        ArrayList<ChessMove> posMoves = new ArrayList<>();

        int promotionRow;
        int placementRow;
        int direction;
        if (actingPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            promotionRow = 8;
            placementRow = 2;
            direction = 1;
        } else {
            promotionRow = 1;
            placementRow = 7;
            direction = -1;
        }

        // 3 cases: strolling forward one, moving two from the placement row, and capturing

        // JUST MOVE 1 CASE (col doesn't change, only onto an empty space)
        int forwardRow = startingRow + direction;
        ChessPosition posPosition = new ChessPosition(forwardRow, startingCol);
        if (board.isInBounds(forwardRow, startingCol) && board.getPiece(posPosition) == null) {
            // if it's a promote row we got to give 4 possible moves, else just the one
            addPawnMove(posMoves, position, posPosition, forwardRow, promotionRow);

            // could also move two if we're on the placement row and that space is open too
            ChessPosition posPosition2 = new ChessPosition(forwardRow + direction, startingCol);
            if (startingRow == placementRow && board.getPiece(posPosition2) == null) {
                posMoves.add(new ChessMove(position, posPosition2, null));
            }
        }

        // CAPTURING CASE (diagonals, only onto an enemy piece)
        int[][] captureDirections = {{direction, 1}, {direction, -1}};
        for (int[] captureDirection : captureDirections) {
            int row = startingRow + captureDirection[0];
            int col = startingCol + captureDirection[1];
            // CHECK BOUNDS
            if (!board.isInBounds(row, col)) {
                continue;
            }
            ChessPosition capturePosition = new ChessPosition(row, col);
            // check now if a piece is in the way, and that it's an enemy we can take
            ChessPiece pieceInWay = board.getPiece(capturePosition);
            if (pieceInWay != null && pieceInWay.getTeamColor() != actingPiece.getTeamColor()) {
                addPawnMove(posMoves, position, capturePosition, row, promotionRow);
            }
        }

        return posMoves;
    }

    // adds a pawn move, expanding into the 4 promotion moves when it reaches the promotion row
    private void addPawnMove(Collection<ChessMove> moves, ChessPosition from,
                             ChessPosition to, int row, int promotionRow) {
        if (row == promotionRow) {
            for (ChessPiece.PieceType promotion : PROMOTION_PIECES) {
                moves.add(new ChessMove(from, to, promotion));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
