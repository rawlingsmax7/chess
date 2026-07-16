package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessPiece.PieceType.*;

public class PawnMovesCalculator implements ChessMovesCalculator {

    static final ChessPiece.PieceType[] promotionPieces = {QUEEN, BISHOP, KNIGHT, ROOK};

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece actingPiece = board.getPiece(position);
        int startingRow = position.getRow();
        int startingCol = position.getColumn();

        ArrayList<ChessMove> posMoves = new ArrayList<ChessMove>();

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

        // 3 cases, at the placement row (could move 1 or 2)
        // just strolling along
        // capturing

        // PLACEMENT ROW
        if (startingRow == placementRow) {
            // we could potentailly move 2, also we don't need to worry about boundaries
            int row = startingRow + direction;
            int col = startingCol; // col doesn't change
            ChessPosition posPosition = new ChessPosition(row, col);
            ChessPiece pieceInWay = board.getPiece(posPosition);
            // check now if a piece is in way
            if (pieceInWay == null) {
                // empty space, add as potential move
                ChessMove posMove = new ChessMove(position, posPosition, null);
                posMoves.add(posMove);

                // could also move two so check that out
                int row2 = row + direction;
                ChessPosition posPosition2 = new ChessPosition(row2, col);
                ChessPiece pieceInWay2 = board.getPiece(posPosition2);
                if (pieceInWay2 == null) {
                    ChessMove posMove2 = new ChessMove(position, posPosition2, null);
                    posMoves.add(posMove2);
                } else {

                }
            } else {

            }
        }
        // JUST MOVE 1 CASE
        else {
            int row = startingRow + direction;
            int col = startingCol; // col doesn't change
            // CHECK BOUNDS
            if (board.isInBounds(row, col)) {
                ChessPosition posPosition = new ChessPosition(row, col);
                ChessPiece pieceInWay = board.getPiece(posPosition);
                if (pieceInWay == null) {
                    // if it's a promote row we got to give 4 possible moves
                    if (row == promotionRow) {
                        for (int i = 0; i < promotionPieces.length; i++) {
                            ChessMove posMove = new ChessMove(position, posPosition, promotionPieces[i]);
                            posMoves.add(posMove);
                        }
                    } else {
                        ChessMove posMove = new ChessMove(position, posPosition, null);
                        posMoves.add(posMove);
                    }
                }
            }
        }
        // CAPTURING CASE
        int[][] captureDirections = {{direction, 1}, {direction, -1}};

        for (int i = 0; i < captureDirections.length; i++) {
            int rowStep = captureDirections[i][0];
            int colStep = captureDirections[i][1];

            int row = startingRow + rowStep;
            int col = startingCol + colStep;

            if (board.isInBounds(row, col)) {
                ChessPosition posPosition = new ChessPosition(row, col);
                ChessPiece pieceInWay = board.getPiece(posPosition);
                // check now if a piece is in way
                if (pieceInWay == null) {
                } else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                } else {
                    if (row == promotionRow) {
                        for (int j = 0; j < promotionPieces.length; j++) {
                            ChessMove posMove = new ChessMove(position, posPosition, promotionPieces[j]);
                            posMoves.add(posMove);
                        }
                    } else {
                        ChessMove posMove = new ChessMove(position, posPosition, null);
                        posMoves.add(posMove);
                    }
                }
            }

        }

        return posMoves;
    }
}
