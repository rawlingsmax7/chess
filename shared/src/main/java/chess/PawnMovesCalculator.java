package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.TeamColor.*;
import static chess.ChessPiece.PieceType.*;

public class PawnMovesCalculator implements ChessMovesCalculator {

    private static final ChessPiece.PieceType[] promotionOptions = {ROOK, KNIGHT, BISHOP, QUEEN};


    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ArrayList<ChessMove> posMoves = new ArrayList<ChessMove>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        ChessPiece actingPiece = board.getPiece(position);


        // get parameters depending on the color
        int direction;
        int startingRow;
        int promotionRow;
        if (actingPiece.getTeamColor() == WHITE) {
            direction = 1;
            startingRow = 2;
            promotionRow = 8;
        }
        else {
            direction = -1;
            startingRow = 7;
            promotionRow = 1;
        }

        // Moving Straight
        // first check the position right in front
        ChessPosition position1 = new ChessPosition(currentRow + direction, currentCol);
        ChessPiece pieceInWay1 = board.getPiece(position1);

        int row1 = position1.getRow();
        int col1 = position1.getColumn();

        if ((1 <= row1 && row1 <= 8) && (1 <= col1 && col1 <= 8)) {
            boolean notBlocked = false;

            if (pieceInWay1 == null) {
                // empty; you can move there
                addLegalOneStepPawnMove(posMoves, position, position1, promotionRow);

                notBlocked = true;
            }
            // check if it can move two
            if (currentRow == startingRow && notBlocked) {
                // then if on the original row check two in front
                ChessPosition position2 = new ChessPosition(currentRow + (2 * direction), currentCol);

                ChessPiece pieceInWay2 = board.getPiece(position2);
                // if we are in starting row then we don't need to check for bounds
                if (pieceInWay2 == null) {
                    ChessMove posMove = new ChessMove(position, position2, null);
                    posMoves.add(posMove);
                }
            }
        }

        // CAPTURING
        int[][] posCaptureDirections = {{direction,1},{direction,-1}};
        for (int i = 0; i < posCaptureDirections.length; i++) {
            int rowStep = posCaptureDirections[i][0];
            int colStep = posCaptureDirections[i][1];

            ChessPosition posPosition = new ChessPosition(currentRow + rowStep, currentCol + colStep);
            int row = posPosition.getRow();
            int col = posPosition.getColumn();

            // check bounds
            if ((1 <= row && row <= 8) && (1 <= col && col <= 8)) {
                ChessPiece pieceInWay = board.getPiece(posPosition);
                if (pieceInWay == null) {

                } else if (pieceInWay.getTeamColor() == actingPiece.getTeamColor()) {
                    // same team
                } else {
                    // enemy team
                    addLegalOneStepPawnMove(posMoves, position, posPosition, promotionRow);
                }
            }
        }

        return posMoves;
    }

    /*
    A valid pawn move is registered so you just need to add depending on if it promotes or not.
     */
    private void addLegalOneStepPawnMove(Collection<ChessMove> possibleMoves, ChessPosition startingPos, ChessPosition endingPos, int promotionRow) {
        int rowEnd = endingPos.getRow();
        if (rowEnd == promotionRow) {
            for (int j = 0; j < promotionOptions.length; j++) {
                ChessMove posMove = new ChessMove(startingPos, endingPos, promotionOptions[j]);
                possibleMoves.add(posMove);
            }
        }
        else {
            ChessMove posMove = new ChessMove(startingPos, endingPos, null);
            possibleMoves.add(posMove);
        }
    }
}
