package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class Rook extends ChessPiece
{
    private boolean hasMoved = false;

    public Rook(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-rook-50.png" : "icons/icons8-rook-50-white.png");
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
        if (absDistanceX > 0 && absDistanceY > 0)
            return false;

        if (isChessPieceInWay(tile))
            return false;

        return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(this.getSide());
    }

    private boolean isChessPieceInWay(final ChessBoard.Tile tile)
    {
        if (tile.getColumn() > super.getTile().getColumn())
        {
            for (int column = super.getTile().getColumn() + 1; column < tile.getColumn(); column++)
            {
                final ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(super.getTile().getRow(), column);
                if (chessPiece != null)
                    return true;
            }
        }
        else if (tile.getColumn() < super.getTile().getColumn())
        {
            for (int column = super.getTile().getColumn() - 1; column > tile.getColumn(); column--)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(super.getTile().getRow(), column) != null)
                    return true;
            }
        }
        else if (tile.getRow() > super.getTile().getRow())
        {
            for (int row = super.getTile().getRow() + 1; row < tile.getRow(); row++)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(row, super.getTile().getColumn()) != null)
                    return true;
            }
        }
        else if (tile.getRow() < super.getTile().getRow())
        {
            for (int row = super.getTile().getRow() - 1; row > tile.getRow(); row--)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(row, super.getTile().getColumn()) != null)
                    return true;
            }
        }

        return false;
    }
}
