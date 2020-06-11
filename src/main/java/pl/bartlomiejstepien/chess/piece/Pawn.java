package pl.bartlomiejstepien.chess.piece;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.List;

public class Pawn extends ChessPiece
{
    boolean isFirstMove = true;

    public Pawn(Side side, ChessboardPosition position)
    {
        super(side, position, side == Side.BLACK ? "icons/icons8-pawn-50.png" : "icons/icons8-pawn-50-white.png");
    }

    @Override
    public void moveTo(final ChessBoard.Tile tile)
    {
        super.moveTo(tile);
        if (isFirstMove)
            isFirstMove = false;

        // Replace pawn with best lost figure if it reaches the end of the chessboard.
        if (tile.getRow() == 8 && super.getSide() == Side.BLACK)
        {
            replaceWithBestLostFigureBlack();
        }
        else if (tile.getRow() == 1 && super.getSide() == Side.WHITE)
        {
            replaceWithBestLostFigureWhite();
        }
    }

    private void replaceWithBestLostFigureWhite()
    {
        final List<ChessPiece> figures = ChessGame.getGame().getAliveWhiteFigures();
        replaceWithBestLostFigure(figures);
    }

    private void replaceWithBestLostFigureBlack()
    {
        final List<ChessPiece> figures = ChessGame.getGame().getAliveBlackFigures();
        replaceWithBestLostFigure(figures);
    }

    private void replaceWithBestLostFigure(final List<ChessPiece> aliveFigures)
    {
        boolean containsQueen = false;
        boolean containsBishop = false;
        boolean containsRook = false;
        boolean containsKnight = false;

//        ChessFigure chessFigure = aliveFigures.get(0);
//            for (int i = 1; i < figures.size(); i++)
//            {
//                if (chessFigure instanceof Queen)
//                    containsQueen = true;
//                else if (chessFigure instanceof Rook)
//                    containsRook = true;
//                else if (chessFigure instanceof Knight)
//                    containsKnight = true;
//                else if (chessFigure instanceof Bishop)
//                    containsBishop = true;
//            }

//            if (!containsQueen)
//            {
//                ChessGame.getGame().getChessBoard().putFigureAtTile(super.getTilePosition().getRow(), super.getTilePosition().getColumn(), new Quuen());
//            }
        System.out.println("Pawn should be replaced with best lost figure but code for it is not yet implemented.");
    }

    @Override
    public boolean canMoveTo(final ChessBoard.Tile newPosition)
    {
        final ChessboardPosition currentPosition = super.getTilePosition();

        if (currentPosition.getRow() == newPosition.getRow() && currentPosition.getColumn() == newPosition.getColumn())
            return false;

        // Pawns can only move one tile at time or two if it is their first move.
        int absDistanceY = Math.abs(newPosition.getRow() - currentPosition.getRow());
        int absDistanceX = Math.abs(newPosition.getColumn() - currentPosition.getColumn());

        if (super.getSide() == Side.WHITE)
        {
            //Go upwards
            if (newPosition.getRow() > currentPosition.getRow())
                return false;
        }
        else
        {
            if (newPosition.getRow() < currentPosition.getRow())
                return false;
        }

        // Validate movement
        final ChessPiece chessPieceAtNewPosition = ChessGame.getGame().getChessBoard().getFigureAt(newPosition.getRow(), newPosition.getColumn());
        if (absDistanceY == 1 && absDistanceX == 0) // One tile (normal move)
        {
            return chessPieceAtNewPosition == null;
        }
        else if (absDistanceY == 2 && isFirstMove && absDistanceX == 0) // Two tiles (first move)
        {
            return chessPieceAtNewPosition == null;
        }
        else if (absDistanceY == 1 && absDistanceX == 1) // Diagonal move (attack)
        {
            return chessPieceAtNewPosition != null && !chessPieceAtNewPosition.getSide().equals(this.getSide());
        }
        else return false;
    }
}
