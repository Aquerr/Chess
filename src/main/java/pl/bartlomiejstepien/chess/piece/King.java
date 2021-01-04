package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.LinkedList;
import java.util.List;

public class King extends ChessPiece
{
    private boolean hasMoved = false;

    public King(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-king-50.png" : "icons/icons8-king-50-white.png");
    }

    @Override
    public boolean canMoveTo(ChessBoard.Tile tile)
    {
        final ChessBoard.Tile currentTile = super.getTile();

        if (currentTile.getRow() == tile.getRow() && currentTile.getColumn() == tile.getColumn())
            return false;

        //TODO: Add special king move.
        // King can only move one tile at time.
        int absDistanceY = Math.abs(tile.getRow() - currentTile.getRow());
        int absDistanceX = Math.abs(tile.getColumn() - currentTile.getColumn());

        boolean canMove = true;
        boolean canDoCastling = false;

        //TODO: Block moving to tiles that can be attacked by enemy chess.

        // Check if king can do castling
        if (absDistanceY == 0 && (absDistanceX == 2 || absDistanceX == 3) && canDoCastling(tile, absDistanceX))
            canDoCastling = true;

        // Validate movement
        if (!canDoCastling && (absDistanceX > 1 || absDistanceY > 1))
            return false;

        final ChessPiece chessPieceAtNewPosition = tile.getChessPiece();
        if (chessPieceAtNewPosition != null && chessPieceAtNewPosition.getSide().equals(this.getSide()))
        {
            canMove = false;
        }

        if (!canMove && !canDoCastling)
            return false;

        // Validate if the king will be threatened at new tile
        ChessGame.getGame().getChessBoard().putFigureAtTile(super.getTile().getRow(), super.getTile().getColumn(), null);
        ChessGame.getGame().getChessBoard().putFigureAtTile(tile.getRow(), tile.getColumn(), this);

        if (willBeThreatenedAtTile(tile))
            canMove = false;

        ChessGame.getGame().getChessBoard().putFigureAtTile(tile.getRow(), tile.getColumn(), chessPieceAtNewPosition);
        ChessGame.getGame().getChessBoard().putFigureAtTile(super.getTile().getRow(), super.getTile().getColumn(), this);
        return canMove;
    }

    @Override
    protected void moveTo(ChessBoard.Tile newTile)
    {
        this.hasMoved = true;

        int absDistanceX = Math.abs(newTile.getColumn() - this.getTile().getColumn());

        // Castling
        if (absDistanceX == 2)
        {
            // Get rook and move it behind the King
            moveRookInCastling(this.getTile(), newTile);
        }

        super.moveTo(newTile);
    }

    public boolean willBeThreatenedAtTile(final ChessBoard.Tile tile)
    {
        List<ChessPiece> chessPieces;
        if (super.getSide() == Side.BLACK)
            chessPieces = ChessGame.getGame().getAliveWhiteFigures();
        else chessPieces = ChessGame.getGame().getAliveBlackFigures();
        for (final ChessPiece chessPiece : chessPieces)
        {
            if (chessPiece.canMoveTo(tile))
            {
                System.out.println(super.getSide().name() + " King will be threatened at tile: " + tile.toString() + " by " + chessPiece.getClass().getName());
                return true;
            }
        }
        return false;
    }

    private boolean canDoCastling(ChessBoard.Tile newTile, int absDistanceX)
    {
        //TODO: Check if enemy piece can attack any of the passed tiles in castling

        if (hasMoved)
            return false;

        if ((absDistanceX == 2))
        {
            final ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(newTile.getRow(), newTile.getColumn());
            if (chessPiece != null)
                return false;

            ChessBoard.Tile currentTile = super.getTile();
            if (currentTile.getColumn() < newTile.getColumn()) // +X
            {
                // Check if tile after newTile has Rook
                final ChessPiece rook = ChessGame.getGame().getChessBoard().getFigureAt(currentTile.getRow(), newTile.getColumn() + 1);
                return rook instanceof Rook && rook.getSide().equals(this.getSide());
            }
            else
            {
                // Check if tile after newTile has Rook
                final ChessPiece rook = ChessGame.getGame().getChessBoard().getFigureAt(currentTile.getRow(), newTile.getColumn() - 1);
                return rook instanceof Rook && rook.getSide().equals(this.getSide());
            }
        }

        return false;
    }

    private void moveRookInCastling(ChessBoard.Tile currentKingTile, ChessBoard.Tile newKingTile)
    {
        final ChessPiece rook;
        if (currentKingTile.getColumn() < newKingTile.getColumn()) // +X
        {
            // Check if tile after newTile has Rook
            rook = ChessGame.getGame().getChessBoard().getFigureAt(currentKingTile.getRow(), newKingTile.getColumn() + 1);
            rook.moveTo(ChessGame.getGame().getChessBoard().getTileAt(newKingTile.getRow(), newKingTile.getColumn() - 1));
        }
        else
        {
            // Check if tile after newTile has Rook
            rook = ChessGame.getGame().getChessBoard().getFigureAt(currentKingTile.getRow(), newKingTile.getColumn() - 1);
            rook.moveTo(ChessGame.getGame().getChessBoard().getTileAt(newKingTile.getRow(), newKingTile.getColumn() + 1));
        }
    }
}
