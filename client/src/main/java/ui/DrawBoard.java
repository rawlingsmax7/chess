package ui;

import chess.ChessBoard;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class DrawBoard {

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;
    private static final int LABEL_GAP = 1;

    // Padded characters.
    private static final String EMPTY = " ";

    private ChessBoard board;
    private PrintStream output;

    String[] numberHeaders = {"8", "7", "6", "5", "4", "3", "2", "1"};


    public DrawBoard(ChessBoard board, PrintStream output) {
        this.board = board;
        this.output = output;
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        new DrawBoard(board, out).draw();
    }

    public void draw() {
        output.print(ERASE_SCREEN);

        drawTopHeaders();
        drawRows();
        drawTopHeaders();

        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_WHITE);
    }

    private void drawTopHeaders() {
        setBlack();

        // first black part
        drawHeader(" ");

        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            drawHeader(headers[boardCol]);

        }

        drawHeader(" ");

        output.print(RESET_BG_COLOR);
        output.println();
    }

    private void drawHeader(String headerText) {
        // one space, one letter, one space
        output.print(EMPTY);
        printHeaderText(headerText);
        output.print(EMPTY);
    }


    private void printHeaderText(String text) {
        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_GREEN);

        output.print(text);

        setBlack();
    }

    private void drawRows() {
        for (int boardRow = 0; boardRow < BOARD_SIZE_IN_SQUARES; ++boardRow) {
            // need to pass which row we are drawing
            drawRowOfSquares(boardRow);
        }
    }

    private void drawRowOfSquares(int boardRow) {
        setBlack();
        drawHeader(numberHeaders[boardRow]);

        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            if ((boardRow + boardCol) % 2 == 0) {
                setWhite();
            } else {
                setBlack();
            }

            // placeholder: 3 blank chars per square. Later, print the piece string here instead.
            output.print(EMPTY.repeat(SQUARE_SIZE_IN_PADDED_CHARS));
        }

        setBlack();
        drawHeader(numberHeaders[boardRow]);

        output.print(RESET_BG_COLOR);
        output.println();
    }

    private void setWhite() {
        output.print(SET_BG_COLOR_WHITE);
        output.print(SET_TEXT_COLOR_WHITE);
    }

    private void setBlack() {
        output.print(SET_BG_COLOR_BLACK);
        output.print(SET_TEXT_COLOR_BLACK);
    }

    private void drawPiece(String piece) {
        output.print(SET_TEXT_COLOR_BLACK);

        output.print(piece);
    }
}
