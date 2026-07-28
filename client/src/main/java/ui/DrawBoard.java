package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class DrawBoard {

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    private static final String EM_SPACE = "\u2003";

    private ChessBoard board;
    private PrintStream output;

    public DrawBoard(ChessBoard board, PrintStream output) {
        this.board = board;
        this.output = output;
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        new DrawBoard(board, out).draw(true);
    }

    public void draw(boolean whitePerspective) {
        output.print(ERASE_SCREEN);

        drawTopHeaders(whitePerspective);
        drawRows(whitePerspective);
        drawTopHeaders(whitePerspective);

        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_WHITE);
    }

    private void drawTopHeaders(boolean whitePerspective) {
        setBlack();

        // blank square at start of row
        output.print(EMPTY);

        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        if (whitePerspective) {
            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; boardCol++) {
                drawHeader(headers[boardCol]);
            }
        } else {
            for (int boardCol = BOARD_SIZE_IN_SQUARES - 1; boardCol >= 0; boardCol--) {
                drawHeader(headers[boardCol]);
            }
        }

        // blank square at end of row
        output.print(EMPTY);

        output.print(RESET_BG_COLOR);
        output.println();
    }

    private void drawHeader(String text) {
        // one narrow space + label + one WIDE em-space = same total width as a square
        output.print(" ");
        printHeaderText(text);
        output.print(EM_SPACE);
    }


    private void printHeaderText(String text) {
        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_GREEN);

        output.print(text);

        setBlack();
    }

    private void drawRows(boolean whitePerspective) {
        // we are actually going to print row 8 first because that's the way the terminal prints
        // and additionally we are doing this so it's easy to access teh ChessBoard object
        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                // need to pass which row we are drawing
                drawRowOfSquares(row, whitePerspective);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                // need to pass which row we are drawing
                drawRowOfSquares(row, whitePerspective);
            }
        }

    }

    private void drawRowOfSquares(int row, boolean whitePerspective) {
        setBlack();
        drawHeader(String.valueOf(row));
        if (whitePerspective) {
            for (int col = 1; col <= 8; col++) {
                if ((row + col) % 2 == 0) {
                    setDarkSquare();
                } else {
                    setLightSquare();
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    output.print(EMPTY);
                } else {
                    printPiece(piece);
                }
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                if ((row + col) % 2 == 0) {
                    setDarkSquare();
                } else {
                    setLightSquare();
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    output.print(EMPTY);
                } else {
                    printPiece(piece);
                }
            }
        }
        setBlack();
        drawHeader(String.valueOf(row));

        output.print(RESET_BG_COLOR);
        output.println();
    }

    private void printPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            output.print(SET_TEXT_COLOR_WHITE);
        } else {
            output.print(SET_TEXT_COLOR_BLACK);
        }

        // just use the black kind because it's a lot clearer
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
            output.print(BLACK_BISHOP);
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            output.print(BLACK_ROOK);
        } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
            output.print(BLACK_QUEEN);
        } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
            output.print(BLACK_KNIGHT);
        } else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            output.print(BLACK_KING);
        }
        // PAWN CASE
        else {
            output.print(BLACK_PAWN);
        }
    }

    // light and dark board squares (grey so both white and black pieces stay readable)
    private void setLightSquare() {
        output.print(SET_BG_COLOR_LIGHT_GREY);
    }

    private void setDarkSquare() {
        output.print(SET_BG_COLOR_DARK_GREY);
    }

    // black background for the header row/column (kept separate from the squares)
    private void setBlack() {
        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_BLACK);
    }
}
