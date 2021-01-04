package pl.bartlomiejstepien.chess;

import javafx.event.EventHandler;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import pl.bartlomiejstepien.chess.piece.ChessPiece;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ChessBoard
{
    private final ChessPiece[][] chessBoardFigures = new ChessPiece[8][8];
    private final Tile[][] chessBoardTiles = new Tile[8][8];

    public Tile[][] getChessBoardTiles()
    {
        return this.chessBoardTiles;
    }

    public List<Tile> getChessBoardTilesAsList()
    {
        final List<Tile> tiles = new LinkedList<>();
        for (final Tile[] row : chessBoardTiles)
        {
            tiles.addAll(Arrays.asList(row));
        }
        return tiles;
    }

    public Tile getTileAt(final int row, final int column)
    {
        try
        {
            return this.chessBoardTiles[row - 1][column - 1];
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public ChessPiece getFigureAt(final int row, final int column)
    {
        try
        {
            return this.chessBoardFigures[row - 1][column - 1];
        }
        catch (Exception e)
        {
            //Outside the chessboard
            return null;
        }
    }

    public Optional<Tile> getIntersectingTile(final double x, final double y)
    {
        for (int row = 0; row < this.chessBoardTiles.length; row++)
        {
            for (int column = 0; column < this.chessBoardTiles[row].length; column++)
            {
                final Tile tile = this.chessBoardTiles[row][column];
                if (tile.getRectangle().intersects(x + 15, y + 15, 30, 30))
                    return Optional.of(tile);
            }
        }
        return Optional.empty();
    }

    public Tile putFigureAtTile(int row, int column, ChessPiece chessPiece)
    {
        this.chessBoardFigures[row - 1][column - 1] = chessPiece;
        this.chessBoardTiles[row - 1][column - 1].setFigure(chessPiece);
        return this.chessBoardTiles[row - 1][column - 1];
    }

    public static final class Tile
    {
        private final int row;
        private final int column;

        private final Rectangle rectangle;

        private ChessPiece chessPiece;

        public Tile(final int row, final int column, final Color color)
        {
            this.row = row;
            this.column = column;

            this.rectangle = new Rectangle(column * 60 - 60, row * 60 - 60, 60, 60);
            this.rectangle.setFill(color);
            rectangle.setStrokeType(StrokeType.CENTERED);

            rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new HighlightTileEventHandler(rectangle, true));
            rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new HighlightTileEventHandler(rectangle, false));

            rectangle.setStroke(Color.BLACK);
        }

        public int getRow()
        {
            return this.row;
        }

        public int getColumn()
        {
            return this.column;
        }

        public Rectangle getRectangle()
        {
            return this.rectangle;
        }

        public ChessPiece getChessPiece()
        {
            return this.chessPiece;
        }

        public void setFigure(ChessPiece chessPiece)
        {
            this.chessPiece = chessPiece;
        }

        public ChessboardPosition getChessboardPosition()
        {
            return new ChessboardPosition(this.row, this.column);
        }

        @Override
        public String toString()
        {
            return "Tile{" +
                    "row=" + row +
                    ", column=" + column +
                    ", rectangle=" + rectangle +
                    '}';
        }
    }

    public static class HighlightTileEventHandler implements EventHandler<MouseEvent>
    {
        private final Rectangle rectangle;
        private final boolean enter;

        public HighlightTileEventHandler(final Rectangle rectangle, final boolean enter)
        {
            this.rectangle = rectangle;
            this.enter = enter;
        }

        @Override
        public void handle(MouseEvent mouseEvent)
        {
            if (enter)
            {
                rectangle.setEffect(new Glow(0.5));
            }
            else
            {
                rectangle.setEffect(null);
            }
        }
    }
}
