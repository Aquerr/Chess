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
import pl.bartlomiejstepien.chess.online.packets.MovePacket;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

        this.rectangle = new Rectangle(ChessBoard.TILE_SIZE, ChessBoard.TILE_SIZE);
        this.rectangle.setX(this.tile.getRectangle().getX());
        this.rectangle.setY(this.tile.getRectangle().getY());

        this.image = new Image(imageUrl);
        this.rectangle.setFill(new ImagePattern(this.image));

        this.lastY = this.rectangle.getY();
        this.lastX = this.rectangle.getX();

        setupDragEvents(this);

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

    private boolean canMoveThisChessSide()
    {
        Side side = ChessGame.getGame().getCurrentMoveSide();
        Side thisChessSide = this.getSide();
        boolean isOnline = ChessGame.getGame().isOnline();

        if (isOnline)
        {
            return ChessGame.getGame().getOnlineConnection().getChessSide().equals(side) && side.equals(thisChessSide);
        }
        else
        {
            return side.equals(thisChessSide);
        }
    }

    private void setupDragEvents(ChessPiece chessPiece)
    {
        Rectangle rectangle = chessPiece.getRectangle();
        rectangle.setOnMousePressed(mouseClickEvent -> {
            if (!canMoveThisChessSide())
                return;

            // Highlight possible movements
            highlightPossibleMovements();
            System.out.println("Mouse Pressed at Tile X: " + tile.getRectangle().getX() + " Y: " + tile.getRectangle().getY() + " Mouse Pos: X: " + (mouseClickEvent.getX()) + " | Y: " + (mouseClickEvent.getY()));
        });

        rectangle.setOnMouseDragged(mouseEvent ->
        {
            //TODO: Improve this... maybe by locking all tiles?
            if (!canMoveThisChessSide())
                return;

            double mouseX = mouseEvent.getX();
            double mouseY = mouseEvent.getY();

            rectangle.setX(mouseX - rectangle.getWidth() / 2);
            rectangle.setY(mouseY - rectangle.getHeight() / 2);
        });

        rectangle.setOnMouseReleased(mouseDragEvent ->
        {
            unHighlightPossibleMovements();

            final int rectangleX = (int)rectangle.getX();
            final int rectangleY = (int)rectangle.getY();

            System.out.println("Mouse Released at Tile: row=" + tile.getRow() + " column=" + tile.getRectangle().getY() + " Mouse Pos: X: " + (mouseDragEvent.getX()) + " | Y: " + (mouseDragEvent.getY()));

            // Get tile the mouse is above
            final Optional<ChessBoard.Tile> optionalTile = ChessGame.getGame().getChessBoard().getIntersectingTile(rectangleX, rectangleY);
            if (optionalTile.isEmpty())
            {
                // Bring figure back to initial position
                rectangle.setX(lastX);
                rectangle.setY(lastY);
                return;
            }

            final ChessBoard.Tile newTile = optionalTile.get();

            // Check if chess figure can move to the tile the mouse is above.
            if (!canMoveTo(newTile))
            {
                // Bring figure back to initial position
                rectangle.setX(lastX);
                rectangle.setY(lastY);
                return;
            }

            if(ChessGame.getGame().isOnline())
            {
                ChessGame.getGame().getOnlineConnection().sendMessage(new MovePacket(this.getTile().getChessboardPosition(), newTile.getChessboardPosition()));
            }

            moveTo(newTile);
        });

        rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(rectangle, true));
        rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(rectangle, false));
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
    public void moveTo(final ChessBoard.Tile newTile)
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
        System.out.printf("Moved %s to %s%n", this.getClass().getSimpleName(), newTile);
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

    @Override
    public String toString()
    {
        return "ChessPiece{" +
                "side=" + side +
                ", rectangle=" + rectangle +
                ", image=" + image +
                ", lastX=" + lastX +
                ", lastY=" + lastY +
                '}';
    }
}
