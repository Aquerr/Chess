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
        final ChessboardPosition currentPosition = super.getTilePosition();
        if (currentPosition.getRow() == tile.getRow() && currentPosition.getColumn() == tile.getColumn())
            return false;

        int absDistanceY = Math.abs(tile.getRow() - currentPosition.getRow());
        int absDistanceX = Math.abs(tile.getColumn() - currentPosition.getColumn());

        // Validate movement
        final ChessPiece chessPieceAtNewPosition = ChessGame.getGame().getChessBoard().getFigureAt(tile.getRow(), tile.getColumn());
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

        final ChessboardPosition ourPosition = super.getTilePosition();

        int newColumn = 0;
        int newRow = 0;

        // Left-down
        if (ourPosition.getColumn() < tile.getColumn() && ourPosition.getRow() > tile.getRow())
        {
            newColumn = tile.getColumn() - 1;
            newRow = tile.getRow() + 1;
        }
        // Left-up
        else if (ourPosition.getColumn() < tile.getColumn() && ourPosition.getRow() < tile.getRow())
        {
            newColumn = tile.getColumn() - 1;
            newRow = tile.getRow() - 1;
        }
        // Right-down
        else if (ourPosition.getColumn() > tile.getColumn() && ourPosition.getRow() > tile.getRow())
        {
            newColumn = tile.getColumn() + 1;
            newRow = tile.getRow() + 1;
        }
        // Right-up
        else if (ourPosition.getColumn() > tile.getColumn() && ourPosition.getRow() < tile.getRow())
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
