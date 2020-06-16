package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class Bishop extends ChessPiece
{
    public Bishop(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-bishop-50.png" : "icons/icons8-bishop-50-white.png");
    }

    @Override
    public boolean canMoveTo(ChessBoard.Tile tile)
    {
        if (ChessGame.getGame().isWhiteMove() && super.getSide() == Side.WHITE && willUncoverKing())
            return false;
        else if (!ChessGame.getGame().isWhiteMove() && super.getSide() == Side.BLACK && willUncoverKing())
            return false;

        final ChessBoard.Tile currentTile = super.getTile();
        if (currentTile.getRow() == tile.getRow() && currentTile.getColumn() == tile.getColumn())
            return false;

        int absDistanceY = Math.abs(tile.getRow() - currentTile.getRow());
        int absDistanceX = Math.abs(tile.getColumn() - currentTile.getColumn());

        // Validate movement
        final ChessPiece chessPieceAtNewPosition = tile.getChessPiece();
        if (absDistanceX != absDistanceY)
            return false;

        if (isChessPieceInWay(tile))
            return false;

        return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(this.getSide());
    }

    private boolean isChessPieceInWay(final ChessBoard.Tile tile)
    {
        if (tile == null)
            return false;

        final ChessBoard.Tile currentTile = super.getTile();

        int newColumn = 0;
        int newRow = 0;

        // Left-down
        if (currentTile.getColumn() < tile.getColumn() && currentTile.getRow() > tile.getRow())
        {
            newColumn = tile.getColumn() - 1;
            newRow = tile.getRow() + 1;
        }
        // Left-up
        else if (currentTile.getColumn() < tile.getColumn() && currentTile.getRow() < tile.getRow())
        {
            newColumn = tile.getColumn() - 1;
            newRow = tile.getRow() - 1;
        }
        // Right-down
        else if (currentTile.getColumn() > tile.getColumn() && currentTile.getRow() > tile.getRow())
        {
            newColumn = tile.getColumn() + 1;
            newRow = tile.getRow() + 1;
        }
        // Right-up
        else if (currentTile.getColumn() > tile.getColumn() && currentTile.getRow() < tile.getRow())
        {
            newColumn = tile.getColumn() + 1;
            newRow = tile.getRow() - 1;
        }

        final ChessPiece chessPieceAtNewTile = ChessGame.getGame().getChessBoard().getFigureAt(newRow, newColumn);
        final ChessBoard.Tile newTile = ChessGame.getGame().getChessBoard().getTileAt(newRow, newColumn);
        if (chessPieceAtNewTile == null)
        {
            return isChessPieceInWay(newTile);
        }
        else
        {
            return !chessPieceAtNewTile.equals(this);
        }
    }
}
