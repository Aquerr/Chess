package pl.bartlomiejstepien.chess;

import javafx.event.EventHandler;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import pl.bartlomiejstepien.chess.piece.ChessPiece;

import java.util.*;
import java.util.function.Function;

public class ChessBoard
{
    public static final int TILE_SIZE = 60;
    public static final int NUMBER_OF_ROWS = 8;

    private final ChessPiece[][] chessBoardFigures = new ChessPiece[NUMBER_OF_ROWS][NUMBER_OF_ROWS];
    private final Tile[][] chessBoardTiles = new Tile[NUMBER_OF_ROWS][NUMBER_OF_ROWS];

    public void putTileAt(int row, int column, Tile tile)
    {
        this.chessBoardTiles[NUMBER_OF_ROWS - row][column - 1] = tile;
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
            return this.chessBoardTiles[NUMBER_OF_ROWS - row][column - 1];
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
            return this.chessBoardFigures[NUMBER_OF_ROWS - row][column - 1];
        }
        catch (Exception e)
        {
            //Outside the chessboard
            return null;
        }
    }

    public Optional<Tile> getIntersectingTile(final double x, final double y)
    {
        for (Tile[] chessBoardTile : this.chessBoardTiles)
        {
            for (final Tile tile : chessBoardTile)
            {
                int tileHalfWidth = TILE_SIZE / 2;
                if (tile.getRectangle().intersects(x + tileHalfWidth, y + tileHalfWidth, tileHalfWidth, tileHalfWidth))
                {
                    return Optional.of(tile);
                }
            }
        }
        return Optional.empty();
    }

    public Tile putFigureAtTile(int row, int column, ChessPiece chessPiece)
    {
        this.chessBoardFigures[NUMBER_OF_ROWS - row][column - 1] = chessPiece;
        this.chessBoardTiles[NUMBER_OF_ROWS - row][column - 1].setFigure(chessPiece);
        return this.chessBoardTiles[NUMBER_OF_ROWS - row][column - 1];
    }

    public static final class Tile
    {
        private final String name;

        private final int row;
        private final int column;

        private final Rectangle rectangle;

        private ChessPiece chessPiece;

        public Tile(final String name, final int row, final int column, final Color color)
        {
            this.name = name;
            this.row = row;
            this.column = column;

            this.rectangle = new Rectangle(column * TILE_SIZE - ChessBoard.TILE_SIZE, NUMBER_OF_ROWS * TILE_SIZE - (row * TILE_SIZE), TILE_SIZE, TILE_SIZE);
            this.rectangle.setFill(color);
            rectangle.setStrokeType(StrokeType.CENTERED);

            rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new HighlightTileEventHandler(rectangle, true));
            rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new HighlightTileEventHandler(rectangle, false));

            rectangle.setStroke(Color.BLACK);
        }

        public String getName()
        {
            return name;
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
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tile tile = (Tile) o;
            return row == tile.row && column == tile.column && name.equals(tile.name) && rectangle.equals(tile.rectangle) && Objects.equals(chessPiece, tile.chessPiece);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, row, column, rectangle, chessPiece);
        }

        @Override
        public String toString()
        {
            return "Tile{" +
                    "name='" + name + '\'' +
                    ", row=" + row +
                    ", column=" + column +
                    ", rectangle=" + rectangle +
                    ", chessPiece=" + chessPiece +
                    '}';
        }
    }

    public static class HighlightTileEventHandler implements EventHandler<MouseEvent>
    {
        public static final Function<Rectangle, Effect> HIGHLIGHT_EFFECT = rectangle -> new Glow(0.5);

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
                rectangle.setEffect(new Blend(BlendMode.ADD, rectangle.getEffect(), HIGHLIGHT_EFFECT.apply(rectangle)));
            }
            else
            {
                rectangle.setEffect(null);
            }
        }
    }
}
