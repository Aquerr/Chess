package pl.bartlomiejstepien.chess.piece;

import javafx.scene.effect.ColorInput;
import javafx.scene.effect.Shadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

public abstract class ChessPiece
{
    private ChessBoard.Tile tile;
    private Side side;

    private Rectangle rectangle;
    private Image image;

    // For dragging
    private double lastX;
    private double lastY;

    protected ChessPiece(final Side side, final ChessboardPosition position, final String imageUrl)
    {
        if (position.getRow() > 8 || position.getRow() < 1)
            throw new IllegalArgumentException("Figure position must be inside 8x8 chessboard. Provided value {column=" + position.getColumn() + "} is outside the board!");
        if (position.getColumn() > 8 || position.getColumn() < 1)
            throw new IllegalArgumentException("Figure position must be inside 8x8 chessboard. Provided value {row=" + position.getRow() + "} is outside the board!");

        this.side = side;
        final ChessGame chessGame = ChessGame.getGame();
        final ChessBoard chessBoard = chessGame.getChessBoard();

        this.tile = chessBoard.putFigureAtTile(position.getRow(), position.getColumn(), this);

        this.rectangle = new Rectangle(60, 60);
        this.rectangle.setFill(Color.WHITE);
        this.rectangle.setX(position.getColumn() * 60 - 60);
        this.rectangle.setY(position.getRow() * 60 - 60);

        this.image = new Image(imageUrl);
        this.rectangle.setFill(new ImagePattern(this.image));

        this.lastY = this.rectangle.getY();
        this.lastX = this.rectangle.getX();

        this.rectangle.setOnMousePressed(mouseClickEvent -> {
            if (!this.getSide().equals(ChessGame.getGame().getCurrentMoveSide()))
                return;

            // Highlight possible movements
            highlightPossibleMovements();
        });

        this.rectangle.setOnMouseDragged(mouseEvent ->
        {
            //TODO: Improve this... maybe by locking all tiles?
            if (!this.getSide().equals(ChessGame.getGame().getCurrentMoveSide()))
                return;

            double mouseX = mouseEvent.getX();
            double mouseY = mouseEvent.getY();

            this.rectangle.setX(mouseX - this.rectangle.getWidth() / 2);
            this.rectangle.setY(mouseY - this.rectangle.getHeight() / 2);
        });

        this.rectangle.setOnMouseReleased(mouseDragEvent ->
        {
            unHighlightPossibleMovements();

            final int rectangleX = (int)this.rectangle.getX();
            final int rectangleY = (int)this.rectangle.getY();

            // Get tile the mouse is above
            final Optional<ChessBoard.Tile> optionalTile = ChessGame.getGame().getChessBoard().getIntersectingTile(rectangleX, rectangleY);
            if (optionalTile.isEmpty())
            {
                // Bring figure back to initial position
                this.rectangle.setX(lastX);
                this.rectangle.setY(lastY);
                return;
            }

            final ChessBoard.Tile newTile = optionalTile.get();

            // Check if chess figure can move to the tile the mouse is above.
            if (!canMoveTo(newTile))
            {
                // Bring figure back to initial position
                this.rectangle.setX(lastX);
                this.rectangle.setY(lastY);
                return;
            }

            System.out.println("Moving chess piece to tile " + tile.getChessboardPosition());
            moveTo(newTile);
        });

        rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(rectangle, true));
        rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(rectangle, false));

        // Add chess piece to alive pieces
        if (this.side == Side.BLACK)
        {
            chessGame.getAliveBlackFigures().add(this);
            chessGame.getChessBoardView().getChildren().add(this.getRectangle());
        }
        else
        {
            chessGame.getAliveWhiteFigures().add(this);
            chessGame.getChessBoardView().getChildren().add(this.getRectangle());

        }
    }

    public Rectangle getRectangle()
    {
        return this.rectangle;
    }

    public Image getImage()
    {
        return image;
    }

    public Side getSide()
    {
        return side;
    }

//    public void setTilePosition(Vector2i tilePosition)
//    {
//        this.lastX = tilePosition.getColumn();
//        this.lastY = tilePosition.getRow();
//        this.rectangle.setX(tilePosition.getColumn());
//        this.rectangle.setY(tilePosition.getRow());
//        final ChessBoard chessBoard = ChessGame.getGame().getChessBoard();
//        chessBoard.getChessBoardFigures()[position.getY()][position.getX()] = this;
//    }

    public ChessBoard.Tile getTile()
    {
        return this.tile;
    }

    /**
     * Override this method to preform additional operations.
     *
     * Include invocation to this method so that the figure will be properly added to the tile.
     *
     * @param newTile the tile that figure should be moved to.
     */
    protected void moveTo(final ChessBoard.Tile newTile)
    {
        if (this instanceof King && ((King) this).willBeThreatenedAtTile(this.tile))
        {
            this.getTile().getRectangle().setEffect(null);
        }

        final ChessGame chessGame = ChessGame.getGame();
        final ChessBoard chessBoard = chessGame.getChessBoard();
        final ChessPiece figureAtTile = newTile.getChessPiece();

        chessGame.destroyPiece(figureAtTile);

        chessBoard.putFigureAtTile(this.tile.getRow(), this.tile.getColumn(), null);
        chessBoard.putFigureAtTile(newTile.getRow(), newTile.getColumn(), this);
        this.tile = newTile;
        this.lastX = newTile.getRectangle().getX();
        this.lastY = newTile.getRectangle().getY();
        this.rectangle.setX(newTile.getRectangle().getX());
        this.rectangle.setY(newTile.getRectangle().getY());

        chessGame.switchSide();

        // Highlight king's tile if it is threatened
        final List<King> kings = Arrays.asList(ChessGame.getGame().getKing(Side.WHITE), ChessGame.getGame().getKing(Side.BLACK));
        for (final King king : kings)
        {
            if (king.willBeThreatenedAtTile(king.getTile()))
            {
                final ChessBoard.Tile kingTile = king.getTile();
                final Rectangle kingTileRectangle = kingTile.getRectangle();
                kingTileRectangle.setEffect(new Shadow(1, Color.RED));
            }
            else
            {
                final ChessBoard.Tile kingTile = king.getTile();
                final Rectangle kingTileRectangle = kingTile.getRectangle();
                kingTileRectangle.setEffect(null);
            }
        }
    }

    protected abstract boolean canMoveTo(final ChessBoard.Tile tile);

    protected boolean willUncoverKing(final ChessBoard.Tile newTile)
    {
        ChessBoard chessBoard = ChessGame.getGame().getChessBoard();

        // Loop over all enemy chess pieces and check if any of them will be able to threat the king
        boolean willKingBeThreatened;

        //Temporary, so that we can use canMoveTo in other chess pieces
        chessBoard.putFigureAtTile(this.tile.getRow(), this.tile.getColumn(), null);
        ChessPiece oldPieceAtNewTile = chessBoard.getFigureAt(newTile.getRow(), newTile.getColumn());
        chessBoard.putFigureAtTile(newTile.getRow(), newTile.getColumn(), this);

        ChessGame.getGame().destroyPiece(oldPieceAtNewTile);
//        if (oldPieceAtNewTile != null)
//        {
//             Destroy enemy figure...
//            if (oldPieceAtNewTile.getSide() == Side.BLACK)
//                ChessGame.getGame().getAliveBlackFigures().remove(oldPieceAtNewTile);
//            else
//                ChessGame.getGame().getAliveWhiteFigures().remove(oldPieceAtNewTile);
//            ChessGame.getGame().getChessBoardView().getChildren().remove(oldPieceAtNewTile.getRectangle());
//        }

        final King king = ChessGame.getGame().getKing(getSide());
        willKingBeThreatened = king.willBeThreatenedAtTile(king.getTile());

        if (oldPieceAtNewTile != null)
        {
            if (oldPieceAtNewTile.getSide() == Side.BLACK)
                ChessGame.getGame().getAliveBlackFigures().add(oldPieceAtNewTile);
            else
                ChessGame.getGame().getAliveWhiteFigures().add(oldPieceAtNewTile);
            ChessGame.getGame().getChessBoardView().getChildren().add(oldPieceAtNewTile.getRectangle());
        }

        chessBoard.putFigureAtTile(newTile.getRow(), newTile.getColumn(), oldPieceAtNewTile);
        chessBoard.putFigureAtTile(this.tile.getRow(), this.tile.getColumn(), this);
        return willKingBeThreatened;
    }

    public void highlightPossibleMovements()
    {
        System.out.println("Highlighting possible movements");
        for (final ChessBoard.Tile tile : ChessGame.getGame().getChessBoard().getChessBoardTilesAsList())
        {
            if (CompletableFuture.completedFuture(canMoveTo(tile)).join())
                tile.getRectangle().setEffect(new ColorInput(tile.getRectangle().getX() + 5, tile.getRectangle().getY() + 5, tile.getRectangle().getWidth() - 10, tile.getRectangle().getHeight() - 10, Color.LIGHTSTEELBLUE));
        }
    }

    public void unHighlightPossibleMovements()
    {
        System.out.println("Hiding possible movements");
        for (final ChessBoard.Tile tile : ChessGame.getGame().getChessBoard().getChessBoardTilesAsList())
        {
            tile.getRectangle().setEffect(null);
        }
    }
}
