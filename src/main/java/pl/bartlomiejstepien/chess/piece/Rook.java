package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class Rook extends ChessPiece
{
    public Rook(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-rook-50.png" : "icons/icons8-rook-50-white.png");
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
        if (absDistanceX > 0 && absDistanceY > 0)
            return false;

        if (isChessPieceInWay(tile))
            return false;

        return chessPieceAtNewPosition == null || !chessPieceAtNewPosition.getSide().equals(this.getSide());
    }

    private boolean isChessPieceInWay(final ChessBoard.Tile tile)
    {
        if (tile.getColumn() > super.getTilePosition().getColumn())
        {
            for (int column = super.getTilePosition().getColumn() + 1; column < tile.getColumn(); column++)
            {
                final ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(super.getTilePosition().getRow(), column);
                if (chessPiece != null)
                    return true;
            }
        }
        else if (tile.getColumn() < super.getTilePosition().getColumn())
        {
            for (int column = super.getTilePosition().getColumn() - 1; column > tile.getColumn(); column--)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(super.getTilePosition().getRow(), column) != null)
                    return true;
            }
        }
        else if (tile.getRow() > super.getTilePosition().getRow())
        {
            for (int row = super.getTilePosition().getRow() + 1; row < tile.getRow(); row++)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(row, super.getTilePosition().getColumn()) != null)
                    return true;
            }
        }
        else if (tile.getRow() < super.getTilePosition().getRow())
        {
            for (int row = super.getTilePosition().getRow() - 1; row > tile.getRow(); row--)
            {
                if (ChessGame.getGame().getChessBoard().getFigureAt(row, super.getTilePosition().getColumn()) != null)
                    return true;
            }
        }

        return false;
    }
}
