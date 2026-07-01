package chess;

import java.util.Arrays;
import java.util.Objects;

import static chess.ChessPiece.PieceType.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    // squares is the board object that is a multi dimensional array to hold chess pieces
    // indices for accessing this array are actually 0-7
    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        // minus one because getRow and getCol get the actual 1 based chess position
        // position is 1 based, while squares is 0 based
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {

        ChessPiece.PieceType[] backRank = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};

        // pawns
        for (int i = 1; i < 9; i++) {
            ChessPosition posWhite = new ChessPosition(2, i);
            ChessPiece pawnWhite = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

            ChessPosition posBlack = new ChessPosition(7, i);
            ChessPiece pawnBlack = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);

            addPiece(posWhite, pawnWhite);
            addPiece(posBlack, pawnBlack);
        }

        // non pawns
        for (int i = 1; i < 9; i++) {
            ChessPosition posWhite = new ChessPosition(1, i);
            ChessPiece pieceWhite = new ChessPiece(ChessGame.TeamColor.WHITE, backRank[i - 1]);

            ChessPosition posBlack = new ChessPosition(8, i);
            ChessPiece pieceBlack = new ChessPiece(ChessGame.TeamColor.BLACK, backRank[i - 1]);

            addPiece(posWhite, pieceWhite);
            addPiece(posBlack, pieceBlack);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
