package pl.bartlomiejstepien.chess;

import javafx.event.EventHandler;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import pl.bartlomiejstepien.chess.entity.ChessFigure;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ChessBoard
{
    private final ChessFigure[][] chessBoardFigures = new ChessFigure[8][8];
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
            for (final Tile tile : row)
            {
                tiles.add(tile);
            }
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

    public ChessFigure getFigureAt(final int row, final int column)
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

//    public boolean isUpOccupied(final Vector2i position)
//    {
//        try
//        {
//            if (this.chessBoardFigures[position.getY() - 1][position.getX()] != null)
//                return true;
//        }
//        catch (Exception e)
//        {
//            return false;
//        }
//        return false;
//    }
//
//    public boolean isDownOccupied(final Vector2i position)
//    {
//        try
//        {
//            if (this.chessBoardFigures[position.getY() + 1][position.getX()] != null)
//                return true;
//        }
//        catch (Exception e)
//        {
//            return false;
//        }
//        return false;
//    }

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

    public void putFigureAtTile(int row, int column, ChessFigure chessFigure)
    {
        this.chessBoardFigures[row - 1][column - 1] = chessFigure;
    }

    public static final class Tile
    {
        private final int row;
        private final int column;

        private final Rectangle rectangle;

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
