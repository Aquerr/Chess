package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.List;

public class King extends ChessPiece
{
    private boolean hasMoved = false;

    public King(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-king-50.png" : "icons/icons8-king-50-white.png");
    }

    public boolean hasMoved()
    {
        return this.hasMoved;
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
        if (absDistanceY == 0 && canDoCastling(tile, absDistanceX))
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
    public void moveTo(ChessBoard.Tile newTile)
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

        if (absDistanceX != 2)
            return false;

        ChessBoard.Tile currentTile = super.getTile();
        if (currentTile.getColumn() < newTile.getColumn()) // +X
        {
            ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(this.getTile().getRow(), 8);
            if (chessPiece != null && chessPiece instanceof Rook)
            {
                Rook rook = (Rook) chessPiece;
                return rook.getSide().equals(this.getSide()) && !rook.hasMoved() && !areTilesInWayToRookAttackedOrOccupied(rook);
            }
            else return false;
        }
        else // -X
        {
            ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(this.getTile().getRow(), 1);
            if (chessPiece != null && chessPiece instanceof Rook)
            {
                Rook rook = (Rook) chessPiece;
                return rook.getSide().equals(this.getSide()) && !rook.hasMoved() && !areTilesInWayToRookAttackedOrOccupied(rook);
            }
            else return false;
        }
    }

    private boolean areTilesInWayToRookAttackedOrOccupied(Rook rook)
    {
        // Check if king is attacked
        if (this.willBeThreatenedAtTile(this.getTile()))
            return true;

        // Check if there are chesspieces in way and if tiles are attacked
        int row = this.getTile().getRow();
        int rookColumn = rook.getTile().getColumn();
        int kingColumn = this.getTile().getColumn();
        int startColumn = rookColumn > kingColumn ? kingColumn + 1 : rookColumn + 1;
        int endColumn = rookColumn > kingColumn ? rookColumn - 1 : kingColumn - 1;

        for (int column = startColumn; column <= endColumn; column++)
        {
            System.out.println("Checking tile at column: " + column);
            ChessBoard.Tile tile = ChessGame.getGame().getChessBoard().getTileAt(row, column);
            if (tile.getChessPiece() != null)
                return true;
            else if (ChessGame.getGame().getAliveFigures(this.getSide().opposite()).stream().anyMatch(chessPiece -> chessPiece.canMoveTo(tile)))
                return true;
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

//    private Rook getPossibleRookForCastling(boolean towardsNegativeX)
//    {
//        int column = this.getTile().getColumn();
//        while (true)
//        {
//            if (towardsNegativeX)
//                column -= 1;
//            else
//                column += 1;
//            ChessBoard.Tile tile = ChessGame.getGame().getChessBoard().getTileAt(this.getTile().getRow(), column);
//            if (tile == null)
//                break;
//
//            ChessPiece chessPieceAtTile = tile.getChessPiece();
//
//            if (chessPieceAtTile instanceof Rook)
//                return (Rook)chessPieceAtTile;
//            else
//                break;
//        }
//
//        return null;
//    }
}
