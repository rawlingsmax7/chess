package chess;

import java.util.Collection;

public interface ChessMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
