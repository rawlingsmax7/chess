package chess;

import java.util.Collection;

public interface ChessMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
