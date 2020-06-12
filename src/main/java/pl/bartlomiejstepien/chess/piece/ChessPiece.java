package pl.bartlomiejstepien.chess.piece;

import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;

import java.util.Optional;

public abstract class ChessPiece
{
    private ChessboardPosition tilePosition;
    private Side side;

    private Rectangle rectangle;
    private Image image;

    // For dragging
    private double lastX;
    private double lastY;

    protected ChessPiece(final Side side, final ChessboardPosition position, final String imageUrl)
    {
        this.tilePosition = position;

        if (this.tilePosition.getRow() > 8 || this.tilePosition.getRow() < 1)
            throw new IllegalArgumentException("Figure position must be inside 8x8 chessboard. Provided value {column=" + this.tilePosition.getColumn() + "} is outside the board!");
        if (this.tilePosition.getColumn() > 8 || this.tilePosition.getColumn() < 1)
            throw new IllegalArgumentException("Figure position must be inside 8x8 chessboard. Provided value {row=" + this.tilePosition.getRow() + "} is outside the board!");

        this.side = side;
        final ChessGame chessGame = ChessGame.getGame();
        final ChessBoard chessBoard = chessGame.getChessBoard();

        chessBoard.putFigureAtTile(tilePosition.getRow(), tilePosition.getColumn(), this);

        this.rectangle = new Rectangle(60, 60);
        this.rectangle.setFill(Color.WHITE);
        this.rectangle.setX(this.tilePosition.getColumn() * 60 - 60);
        this.rectangle.setY(this.tilePosition.getRow() * 60 - 60);
//        this.rectangle.setStroke(Color.BLACK);

        this.image = new Image(imageUrl);
        this.rectangle.setFill(new ImagePattern(this.image));

        this.lastY = this.rectangle.getY();
        this.lastX = this.rectangle.getX();

        this.rectangle.setOnMousePressed(mouseClickEvent -> {
            if (this.getSide() == Side.BLACK && ChessGame.getGame().isWhiteMove())
                return;
            else if (this.getSide() == Side.WHITE && !ChessGame.getGame().isWhiteMove())
                return;

            // Highlight possible movements
            highlightPossibleMovements();
        });

        this.rectangle.setOnMouseDragged(mouseEvent ->
        {
            //TODO: Improve this... maybe by locking all tiles?
            if (this.getSide() == Side.BLACK && ChessGame.getGame().isWhiteMove())
                return;
            else if (this.getSide() == Side.WHITE && !ChessGame.getGame().isWhiteMove())
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

            final ChessBoard.Tile tile = optionalTile.get();

            // Check if chess figure can move to the tile the mouse is above.
            if (!canMoveTo(tile))
            {
                // Bring figure back to initial position
                this.rectangle.setX(lastX);
                this.rectangle.setY(lastY);
                return;
            }

            moveTo(tile);
        });

        // Add chess piece to alive pieces
        if (this.side == Side.BLACK)
            chessGame.getAliveBlackFigures().add(this);
        else chessGame.getAliveWhiteFigures().add(this);
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

    public ChessboardPosition getTilePosition()
    {
        return this.tilePosition;
    }

    /**
     * Override this method to preform additional operations.
     *
     * Include invocation to this method so that the figure will be properly added to the tile.
     *
     * @param tile the tile that figure should be moved to.
     */
    protected void moveTo(final ChessBoard.Tile tile)
    {
        final ChessGame chessGame = ChessGame.getGame();
        final ChessBoard chessBoard = chessGame.getChessBoard();
        final ChessPiece figureAtTile = chessBoard.getFigureAt(tile.getRow(), tile.getColumn());

        if (figureAtTile != null)
        {
            // Destroy enemy figure...
            if (figureAtTile.getSide() == Side.BLACK)
                chessGame.getAliveBlackFigures().remove(figureAtTile);
            else
                chessGame.getAliveWhiteFigures().remove(figureAtTile);
            chessGame.getChessBoardView().getChildren().remove(figureAtTile.getRectangle());
        }

        chessBoard.putFigureAtTile(this.tilePosition.getRow(), this.tilePosition.getColumn(), null);
        chessBoard.putFigureAtTile(tile.getRow(), tile.getColumn(), this);
        this.tilePosition = ChessboardPosition.from(tile.getRow(), tile.getColumn());
        this.lastX = tile.getRectangle().getX();
        this.lastY = tile.getRectangle().getY();
        this.rectangle.setX(tile.getRectangle().getX());
        this.rectangle.setY(tile.getRectangle().getY());

        chessGame.setWhiteMove(this.getSide() == Side.BLACK);
    }

    public abstract boolean canMoveTo(final ChessBoard.Tile tile);

//    public abstract boolean canAttack(final ChessboardPosition position);

    public void highlightPossibleMovements()
    {
        for (final ChessBoard.Tile tile :  ChessGame.getGame().getChessBoard().getChessBoardTilesAsList())
        {
            if (canMoveTo(tile))
                tile.getRectangle().setEffect(new ColorInput(tile.getRectangle().getX() + 5, tile.getRectangle().getY() + 5, tile.getRectangle().getWidth() - 10, tile.getRectangle().getHeight() - 10, Color.LIGHTSTEELBLUE));
        }
    }
    public void unHighlightPossibleMovements()
    {
        for (final ChessBoard.Tile tile :  ChessGame.getGame().getChessBoard().getChessBoardTilesAsList())
        {
            if (canMoveTo(tile))
                tile.getRectangle().setEffect(null);
        }
    }
}
