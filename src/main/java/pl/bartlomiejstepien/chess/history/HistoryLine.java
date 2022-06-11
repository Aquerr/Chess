package pl.bartlomiejstepien.chess.history;

import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.piece.ChessPiece;
import pl.bartlomiejstepien.chess.piece.Side;

public class HistoryLine
{
    private String chessPiece;
    private Side side;
    private String tile;

    public HistoryLine(ChessPiece chessPiece, ChessBoard.Tile tile)
    {
        this.side = chessPiece.getSide();
        this.chessPiece = chessPiece.getClass().getSimpleName();
        this.tile = tile.getName();
    }

    public Side getSide()
    {
        return side;
    }

    public String getChessPiece()
    {
        return chessPiece;
    }

    public String asAlgebraicNotation()
    {
        return chessPiece.charAt(0) + tile;
    }

    public String getTile()
    {
        return tile;
    }

    @Override
    public String toString()
    {
        return "HistoryLine{" +
                "chessPiece='" + chessPiece + '\'' +
                ", side=" + side +
                ", tile='" + tile + '\'' +
                '}';
    }
}
