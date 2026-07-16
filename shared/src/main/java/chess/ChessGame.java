package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
    private ChessPosition enPassantTarget = null; // this is where you would move your piece if you want to initiate an enPassant move

    // castling fields
    private boolean blackKingUnmoved = true;
    private boolean whiteKingUnmoved = true;
    private boolean blackRookUnmovedFile1 = true;
    private boolean whiteRookUnmovedFile1 = true;
    private boolean blackRookUnmovedFile8 = true;
    private boolean whiteRookUnmovedFile8 = true;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board.resetBoard();
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

    private void changeTeamTurn() {
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
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

        // EN PASSANT
        // if the piece is a pawn and there's an active enPassantTarget then we need to add that to the valid moves
        if (actingPiece.getPieceType() == ChessPiece.PieceType.PAWN && enPassantTarget != null) {
            int direction;
            if (actingPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1;
            } else {
                direction = -1;
            }

            // need to see from which positions the acting Piece could actually capture from, then create those positions
            int enPassantRow = startPosition.getRow() + direction;
            int enPassantCol1 = startPosition.getColumn() + 1;
            int enPassantCol2 = startPosition.getColumn() - 1;

            ChessPosition posEndPosition1 = new ChessPosition(enPassantRow, enPassantCol1);
            ChessPosition posEndPosition2 = new ChessPosition(enPassantRow, enPassantCol2);

            // then check if either of those positions are equal to the actual enPassant target
            if (posEndPosition1.equals(enPassantTarget)) {
                // if it equals then it's a possible move
                ChessMove enPassantMove = new ChessMove(startPosition, enPassantTarget, null);
                pieceMoves.add(enPassantMove);
            }
            if (posEndPosition2.equals(enPassantTarget)) {
                ChessMove enPassantMove = new ChessMove(startPosition, enPassantTarget, null);
                pieceMoves.add(enPassantMove);
            }
        }

        // CASTLING — only for the acting king, on its own home square
        if (actingPiece.getPieceType() == ChessPiece.PieceType.KING &&
                team == TeamColor.WHITE &&
                startPosition.equals(new ChessPosition(1, 5))) {
            // white queenside
            addCastleMove(TeamColor.WHITE, whiteKingUnmoved && whiteRookUnmovedFile1, 1, new int[]{4, 3, 2}, new int[]{4, 3}, 3, validMoves);
            // white kingside
            addCastleMove(TeamColor.WHITE, whiteKingUnmoved && whiteRookUnmovedFile8, 1, new int[]{6, 7}, new int[]{6, 7}, 7, validMoves);
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.KING &&
                team == TeamColor.BLACK &&
                startPosition.equals(new ChessPosition(8, 5))) {
            // black queenside
            addCastleMove(TeamColor.BLACK, blackKingUnmoved && blackRookUnmovedFile1, 8, new int[]{4, 3, 2}, new int[]{4, 3}, 3, validMoves);
            // black kingside
            addCastleMove(TeamColor.BLACK, blackKingUnmoved && blackRookUnmovedFile8, 8, new int[]{6, 7}, new int[]{6, 7}, 7, validMoves);
        }

        // simulate the moves
        // got the potential moves, now need to change the board to the actual move and see if the king is in Check
        {
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
        }
        // don't put these in the same if statement because we need to check
        TeamColor actingPieceColor = actingPiece.getTeamColor();
        if (actingPieceColor != teamTurn) {
            throw new InvalidMoveException();
        } else {
            // get the valid moves
            Collection<ChessMove> validMoves = validMoves(startingPos);

            // check if the move's end position is contained in validMoves, and it's an enPassant move
            if (validMoves.contains(move) && move.getEndPosition().equals(enPassantTarget)) {
                int direction;
                if (actingPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    direction = 1;
                } else {
                    direction = -1;
                }
                board.addPiece(enPassantTarget, actingPiece);
                // remove one row in opposite of direction to take piece off board
                ChessPosition behindEnPassantTarget = new ChessPosition(enPassantTarget.getRow() - direction, enPassantTarget.getColumn());

                // clear the enemy piece and also the previous position of the acting Pawn
                board.addPiece(behindEnPassantTarget, null);
                board.addPiece(startingPos, null);
                changeTeamTurn();

            }

            // need to see if the move is in validMoves
            else if (validMoves.contains(move)) {
                // move is valid so we can perform it
                ChessPosition endingPos = move.getEndPosition();

                // if it's a promotion then we have to worry about that case
                // if the promotionPiece part of the move is null then we don't have to worry about promotion
                if (move.getPromotionPiece() == null) {
                    board.addPiece(endingPos, actingPiece);
                } else { // promotion case
                    ChessGame.TeamColor team = actingPiece.getTeamColor();
                    ChessPiece promotedPiece = new ChessPiece(team, move.getPromotionPiece());
                    board.addPiece(endingPos, promotedPiece);

                }
                // remove the piece and change the turn
                board.addPiece(startingPos, null);

                // CASTLING
                int colDifference = endingPos.getColumn() - startingPos.getColumn(); // if this is positive then it's kingside castling
                // the above code moves the king, but we also then need to move the rook
                if (actingPiece.getPieceType() == ChessPiece.PieceType.KING && 2 == (Math.abs(startingPos.getColumn() - endingPos.getColumn()))) {
                    if (colDifference > 0 && actingPieceColor == TeamColor.WHITE) {
                        // WHITE KINGSIDE
                        board.addPiece(new ChessPosition(1, 6), new ChessPiece(actingPieceColor, ChessPiece.PieceType.ROOK));
                        board.addPiece(new ChessPosition(1, 8), null);
                    } else if (colDifference < 0 && actingPieceColor == TeamColor.WHITE) {
                        // WHITE QUEENSIDE
                        board.addPiece(new ChessPosition(1, 4), new ChessPiece(actingPieceColor, ChessPiece.PieceType.ROOK));
                        board.addPiece(new ChessPosition(1, 1), null);
                    } else if (colDifference > 0 && actingPieceColor == TeamColor.BLACK) {
                        // BLACK KINGSIDE
                        board.addPiece(new ChessPosition(8, 6), new ChessPiece(actingPieceColor, ChessPiece.PieceType.ROOK));
                        board.addPiece(new ChessPosition(8, 8), null);
                    } else if (colDifference < 0 && actingPieceColor == TeamColor.BLACK) {
                        // BLACK QUEENSIDE
                        board.addPiece(new ChessPosition(8, 4), new ChessPiece(actingPieceColor, ChessPiece.PieceType.ROOK));
                        board.addPiece(new ChessPosition(8, 1), null);
                    }
                }
                changeTeamTurn();
            } else {
                // validMoves doesn't contain the move so throw an InvalidMoveException
                throw new InvalidMoveException();
            }
        }

        // check if the move that happened was a pawn and it was a 2 row move; if so then there is now an enPassantTarget
        ChessPosition endingPos = move.getEndPosition();
        int row_difference = Math.abs(startingPos.getRow() - endingPos.getRow());

        int enPassantRow = (startingPos.getRow() + endingPos.getRow()) / 2;
        if (actingPiece.getPieceType() == ChessPiece.PieceType.PAWN && row_difference == 2) {
            enPassantTarget = new ChessPosition(enPassantRow, startingPos.getColumn());
        } else {
            enPassantTarget = null;
        }

        // CASTLING
        // check if there was a king or rook move that then made castling invalid for a team
        // KINGS
        if (actingPiece.getPieceType() == ChessPiece.PieceType.KING && actingPieceColor == TeamColor.WHITE) {
            whiteKingUnmoved = false;
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.KING && actingPieceColor == TeamColor.BLACK) {
            blackKingUnmoved = false;
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.ROOK && actingPieceColor == TeamColor.WHITE && startingPos.getColumn() == 1) {
            whiteRookUnmovedFile1 = false;
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.ROOK && actingPieceColor == TeamColor.BLACK && startingPos.getColumn() == 1) {
            blackRookUnmovedFile1 = false;
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.ROOK && actingPieceColor == TeamColor.WHITE && startingPos.getColumn() == 8) {
            whiteRookUnmovedFile8 = false;
        }
        if (actingPiece.getPieceType() == ChessPiece.PieceType.ROOK && actingPieceColor == TeamColor.BLACK && startingPos.getColumn() == 8) {
            blackRookUnmovedFile8 = false;
        }


    }

    private void addCastleMove(TeamColor team, boolean canCastle, int row, int[] emptyCols,
                               int[] kingPathCols, int kingEndCol, Collection<ChessMove> validMoves) {
        if (!canCastle || isInCheck(team)) {
            return;
        }

        ChessPosition kingStart = new ChessPosition(row, 5);
        ChessPiece king = board.getPiece(kingStart);

        // all squares between king and rook must be empty
        for (int col : emptyCols) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return;
            }
        }

        // king can't be in check while moving to castle
        for (int col : kingPathCols) {
            ChessPosition position = new ChessPosition(row, col);
            board.addPiece(position, king);
            board.addPiece(kingStart, null);
            boolean crossedCheck = isInCheck(team);
            

            board.addPiece(position, null);
            board.addPiece(kingStart, king);
            if (crossedCheck) {
                // if the move would've been in check we can't add it so just return
                return;
            }
        }

        // if passes those tests then we can add the castling move
        validMoves.add(new ChessMove(kingStart, new ChessPosition(row, kingEndCol), null));
    }

    /**
     * Finds position of king for a certain team
     *
     * @param teamColor which team to check for check
     * @return ChessPosition where king is located
     */
    private ChessPosition findKing(TeamColor teamColor) {
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
                    return position;
                }
            }
        }
        // if no king found by some odd means return null
        return null;
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

        ChessPosition actingPosition = findKing(teamColor);

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
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean teamHasNoValidMoves(TeamColor teamColor) {
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
        // if we swept through board and there weren't any valid moves then return true
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return (isInCheck(teamColor) && teamHasNoValidMoves(teamColor));
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if we are in check then we can't be in stalemate
        return (!isInCheck(teamColor) && teamHasNoValidMoves(teamColor));
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
