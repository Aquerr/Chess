package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class King extends ChessPiece
{

    public King(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-king-50.png" : "icons/icons8-king-50-white.png");
    }

    @Override
    public boolean canMoveTo(ChessBoard.Tile newPosition)
    {
        final ChessboardPosition currentPosition = super.getTilePosition();

        if (currentPosition.getRow() == newPosition.getRow() && currentPosition.getColumn() == newPosition.getColumn())
            return false;

        //TODO: Add special king move.
        // King can only move one tile at time.
        int absDistanceY = Math.abs(newPosition.getRow() - currentPosition.getRow());
        int absDistanceX = Math.abs(newPosition.getColumn() - currentPosition.getColumn());

        //TODO: Block moving to tiles that can be attacked by enemy chess.
        // Validate movement
        final ChessPiece chessPieceAtNewPosition = ChessGame.getGame().getChessBoard().getFigureAt(newPosition.getRow(), newPosition.getColumn());
        if (absDistanceX > 1 || absDistanceY > 1)
            return false;

        return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(this.getSide());
    }
}
