package pl.bartlomiejstepien.chess.piece;

import javafx.application.Platform;
import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

public class Pawn extends ChessPiece
{
    private boolean isFirstMove = true;
    private boolean lastMoveWasDoubleMove = false;

    public Pawn(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-pawn-50.png" : "icons/icons8-pawn-50-white.png");
    }

    @Override
    public void moveTo(final ChessBoard.Tile newTile)
    {
        if (isFirstMove)
        {
            int absDistanceY = Math.abs(newTile.getRow() - super.getTile().getRow());
            if (absDistanceY == 2)
                lastMoveWasDoubleMove = true;
            else
                lastMoveWasDoubleMove = false;
            isFirstMove = false;
        }

        if (canDoEnPassant(newTile))
        {
            destroyPawnInEnPassant(newTile);
        }

        if (newTile.getRow() == 1 && super.getSide() == Side.BLACK)
        {
            replaceWithChosenChessPiece(newTile);
        }
        else if (newTile.getRow() == 8 && super.getSide() == Side.WHITE)
        {
            replaceWithChosenChessPiece(newTile);
        }

        super.moveTo(newTile);
    }

    private void replaceWithChosenChessPiece(final ChessBoard.Tile newTile)
    {
        Platform.runLater(() -> ChessGame.getGame().showPawnReplacementWindow(this.getSide(), newTile));
    }

    @Override
    public boolean canMoveTo(final ChessBoard.Tile newTile)
    {
        if (ChessGame.getGame().getCurrentMoveSide().equals(super.getSide()) && willUncoverKing(newTile))
            return false;

        final ChessBoard.Tile currentTile = super.getTile();

        if (currentTile.getRow() == newTile.getRow() && currentTile.getColumn() == newTile.getColumn())
            return false;

        // Pawns can only move one tile at time or two if it is their first move.
        int absDistanceY = Math.abs(newTile.getRow() - currentTile.getRow());
        int absDistanceX = Math.abs(newTile.getColumn() - currentTile.getColumn());

        if (super.getSide() == Side.WHITE)
        {
            //Go upwards
            if (newTile.getRow() < currentTile.getRow())
                return false;
        }
        else
        {
            if (newTile.getRow() > currentTile.getRow())
                return false;
        }

        // Validate movement
        final ChessPiece chessPieceAtNewPosition = newTile.getChessPiece();
        if (absDistanceY == 1 && absDistanceX == 0) // One tile (normal move)
        {
            return chessPieceAtNewPosition == null;
        }
        else if (canMoveTwoTiles(newTile, absDistanceX, absDistanceY))// Two tiles (first move)
        {
            return chessPieceAtNewPosition == null;
        }
        else if (absDistanceY == 1 && absDistanceX == 1) // Diagonal move (attack)
        {
            if(chessPieceAtNewPosition != null && !chessPieceAtNewPosition.getSide().equals(this.getSide())) // Normal attack
                return true;
            else return canDoEnPassant(newTile); // En passant attack
        }
        else return false;
    }

    private void destroyPawnInEnPassant(ChessBoard.Tile newTile)
    {
        ChessPiece chessPiece;
        if (getSide() == Side.WHITE)
        {
            chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(newTile.getRow() - 1, newTile.getColumn());
        }
        else
        {
            chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(newTile.getRow() + 1, newTile.getColumn());
        }

        if (chessPiece != null)
        {
            ChessGame.getGame().destroyPiece(chessPiece);
        }
    }

    private boolean canDoEnPassant(ChessBoard.Tile newTile)
    {
        int absDistanceY = Math.abs(newTile.getRow() - getTile().getRow());
        int absDistanceX = Math.abs(newTile.getColumn() - getTile().getColumn());

        if (absDistanceY == 1 && absDistanceX == 1)
        {
            ChessPiece chessPiece;
            if (getSide() == Side.WHITE)
            {
                chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(newTile.getRow() - 1, newTile.getColumn());
            }
            else
            {
                chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(newTile.getRow() + 1, newTile.getColumn());
            }
            return chessPiece instanceof Pawn && ((Pawn) chessPiece).isLastMoveDoubleMove();
        }
        return false;
    }

    private boolean canMoveTwoTiles(final ChessBoard.Tile newTile, int absDistanceX, int absDistanceY)
    {
        if (!isFirstMove || (absDistanceY != 2 || absDistanceX != 0))
            return false;

        if (super.getSide() == Side.WHITE)
        {
            if (newTile.getRow() > super.getTile().getRow())
            {
                final ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(super.getTile().getRow() + 1, super.getTile().getColumn());
                return chessPiece == null;
            }
        }
        else
        {
            if (newTile.getRow() < super.getTile().getRow())
            {
                final ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(super.getTile().getRow() - 1, super.getTile().getColumn());
                return chessPiece == null;
            }
        }

        return false;
    }

    public boolean getIsFirstMove()
    {
        return this.isFirstMove;
    }

    public boolean isLastMoveDoubleMove()
    {
        return this.lastMoveWasDoubleMove;
    }
}
