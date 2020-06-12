package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.List;

public class King extends ChessPiece
{

    public King(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-king-50.png" : "icons/icons8-king-50-white.png");
    }

    @Override
    public boolean canMoveTo(ChessBoard.Tile tile)
    {
        final ChessboardPosition currentPosition = super.getTilePosition();

        if (currentPosition.getRow() == tile.getRow() && currentPosition.getColumn() == tile.getColumn())
            return false;

        //TODO: Add special king move.
        // King can only move one tile at time.
        int absDistanceY = Math.abs(tile.getRow() - currentPosition.getRow());
        int absDistanceX = Math.abs(tile.getColumn() - currentPosition.getColumn());

        boolean canMove = true;

        //TODO: Block moving to tiles that can be attacked by enemy chess.
        // Validate movement
        final ChessPiece chessPieceAtNewPosition = ChessGame.getGame().getChessBoard().getFigureAt(tile.getRow(), tile.getColumn());
        if (absDistanceX > 1 || absDistanceY > 1)
            return false;

        if (chessPieceAtNewPosition != null && chessPieceAtNewPosition.getSide().equals(this.getSide()))
        {
            canMove = false;
        }

        if (!canMove)
            return false;

        // Validate if the king will be threatened at new tile
        if (willBeThreatenedAtTile(tile))
            canMove = false;

        return canMove;
    }

    private boolean willBeThreatenedAtTile(final ChessBoard.Tile tile)
    {
        List<ChessPiece> chessPieces;
        if (super.getSide() == Side.BLACK)
            chessPieces = ChessGame.getGame().getAliveWhiteFigures();
        else chessPieces = ChessGame.getGame().getAliveBlackFigures();
        for (final ChessPiece chessPiece : chessPieces)
        {
            if (chessPiece.canMoveTo(tile))
                return true;
        }
        return false;
    }
}
