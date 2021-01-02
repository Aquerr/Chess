package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class Knight extends ChessPiece
{
    public Knight(Side side, ChessboardPosition tilePosition)
    {
        super(side, tilePosition, side == Side.BLACK ? "icons/icons8-knight-50.png" : "icons/icons8-knight-50-white.png");
    }

    @Override
    public boolean canMoveTo(ChessBoard.Tile tile)
    {
        if (ChessGame.getGame().isWhiteMove() && super.getSide() == Side.WHITE && willUncoverKing(tile))
            return false;
        else if (!ChessGame.getGame().isWhiteMove() && super.getSide() == Side.BLACK && willUncoverKing(tile))
            return false;

        final ChessBoard.Tile currentTile = super.getTile();

        if (currentTile.getRow() == tile.getRow() && currentTile.getColumn() == tile.getColumn())
            return false;

        int absDistanceY = Math.abs(tile.getRow() - currentTile.getRow());
        int absDistanceX = Math.abs(tile.getColumn() - currentTile.getColumn());

        // Validate movement
        final ChessPiece chessPieceAtNewPosition = tile.getChessPiece();

        if (absDistanceY == 2 && absDistanceX == 1)
            return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(super.getSide());
        else if (absDistanceX == 2 && absDistanceY == 1)
            return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(super.getSide());
        return false;
    }
}
