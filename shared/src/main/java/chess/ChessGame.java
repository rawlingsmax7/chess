package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    // fields here
    private TeamColor teamTurn;
    private ChessBoard board = new ChessBoard();


    public ChessGame() {
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece actingPiece = board.getPiece(startPosition);
        if (actingPiece == null) {
            return null;
        }

        TeamColor team = actingPiece.getTeamColor();

        Collection<ChessMove> pieceMoves = actingPiece.pieceMoves(board, startPosition);
        // got the potential moves, now need to change the board to the actual move and see if the king is in Check
        for (ChessMove move : pieceMoves) {
            ChessPosition endingPos = move.getEndPosition();

            // need to get the piece which could be at the ending position
            ChessPiece temporaryPiece = board.getPiece(endingPos);

            // now we can add the acting piece to the endposition and make the starting part null
            board.addPiece(endingPos, actingPiece);
            board.addPiece(startPosition, null);

            // if the team is not in check then it's a valid move; add it
            if (!isInCheck(team)) {
                validMoves.add(move);
            }

            // return board to original state
            board.addPiece(endingPos, temporaryPiece);
            board.addPiece(startPosition, actingPiece);
        }
        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // need to get the piece at the start of the move and see where it can go
        ChessPosition startingPos = move.getStartPosition();
        ChessPiece actingPiece = board.getPiece(startingPos);

        if (actingPiece == null) {
            throw new InvalidMoveException();
        } else {
//            Collection<ChessMove> posMoves = ChessPiece.pieceMoves(board, startingPos);
        }


    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // need to sweep through the whole board to find the position of the king
        // then need to sweep through all the enemy pieces to get their potential moves
        // add all those moves to an array
        // see if the ending position of those moves is where the king currently is, if any match, it's in check

        // start it off with not in check
        boolean inCheck = false;

        ChessPosition actingPosition = null;

        // sweep through the whole board to find the position of the king
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);

                ChessPiece possibleKing = board.getPiece(position);

                // if the piece is actually an empty piece then we skip
                if (possibleKing == null) {
                    // skip the loop if the space is empty
                    continue;
                }
                // if the piece we are looking at is a king and the same teamColor this is the one we want
                else if (possibleKing.getPieceType() == ChessPiece.PieceType.KING && possibleKing.getTeamColor() == teamColor) {
                    actingPosition = position;
                }

            }
        }

        // now sweep the board for every enemy piece
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);

                ChessPiece possibleEnemy = board.getPiece(position);

                // if the piece is actually an empty piece then we skip
                if (possibleEnemy == null) {
                    // skip the loop if the space is empty
                    continue;
                }
                // if the piece we are looking at isn't the same team we want to get the moves of that one
                else if (possibleEnemy.getTeamColor() != teamColor) {
                    Collection<ChessMove> enemyMoves = possibleEnemy.pieceMoves(board, position);
                    for (ChessMove move : enemyMoves) {
                        ChessPosition targetedPosition = move.getEndPosition();
                        // if the ending position of one of the moves is where the king is at then it's in check
                        if (targetedPosition.equals(actingPosition)) {
                            inCheck = true;
                            return inCheck;
                        }
                    }
                }
            }
        }
        return inCheck;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // first you need to be in check
        if (isInCheck(teamColor)) {
            // sweep through board to get valid positions for each piece
            for (int row = 1; row < 9; row++) {
                for (int col = 1; col < 9; col++) {
                    ChessPosition position = new ChessPosition(row, col);

                    ChessPiece possiblePiece = board.getPiece(position);

                    // if the board is empty there then skip it
                    if (possiblePiece == null) {
                        continue;
                    }
                    // if the board is the same team then see if we can move it
                    else if (possiblePiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> validMoves = validMoves(position);
                        // if a piece ever has a valid move then we aren't in checkmate so can return false
                        if (!validMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            // if we looked at all the moves and they were all empty then there's no valid moves so we are in checkmate
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return false;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
