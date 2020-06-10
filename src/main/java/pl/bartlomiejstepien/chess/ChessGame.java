package pl.bartlomiejstepien.chess;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pl.bartlomiejstepien.chess.entity.ChessFigure;
import pl.bartlomiejstepien.chess.entity.King;
import pl.bartlomiejstepien.chess.entity.Pawn;
import pl.bartlomiejstepien.chess.entity.Side;

import java.util.ArrayList;
import java.util.List;

public class ChessGame extends Application
{
    private static ChessGame INSTANCE;

    private final ChessBoard chessBoard;

    private Stage stage;
    private Scene scene;
    private Group root;
    private Group chessBoardGroup;

    private List<ChessFigure> aliveWhiteFigures = new ArrayList<>();
    private List<ChessFigure> aliveBlackFigures = new ArrayList<>();

    private boolean isWhiteMove = true;

    public ChessGame()
    {
        INSTANCE = this;
        this.chessBoard = new ChessBoard();
    }

    public static ChessGame getGame()
    {
        return INSTANCE;
    }

    public boolean isWhiteMove()
    {
        return this.isWhiteMove;
    }

    public void setWhiteMove(boolean whiteMove)
    {
        isWhiteMove = whiteMove;
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage = primaryStage;
        this.root = new Group();
        this.scene = new Scene(root, 600, 600);

        this.chessBoardGroup = new Group();
        root.getChildren().add(chessBoardGroup);
        chessBoardGroup.setTranslateX(60);
        chessBoardGroup.setTranslateY(60);

        // Draw chessboard
        drawChessboard();
        setupChessFigures();

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupChessFigures()
    {
        // Pawns
        for (int i = 1; i <= 8; i++)
        {
            final Pawn blackPawn = new Pawn(Side.BLACK, new ChessboardPosition(2, i));
            final Pawn whitePawn = new Pawn(Side.WHITE, new ChessboardPosition(7, i));
            this.aliveBlackFigures.add(blackPawn);
            this.aliveWhiteFigures.add(whitePawn);
        }

        final King blackKing = new King(Side.BLACK, new ChessboardPosition(1,5));
        final King whiteKing = new King(Side.WHITE, new ChessboardPosition(8,5));
        this.aliveBlackFigures.add(blackKing);
        this.aliveWhiteFigures.add(whiteKing);

        // Add figure rectangles/boxes to view
        for (final ChessFigure chessFigure : this.aliveWhiteFigures)
        {
            this.chessBoardGroup.getChildren().add(chessFigure.getRectangle());
        }

        for (final ChessFigure chessFigure : this.aliveBlackFigures)
        {
            this.chessBoardGroup.getChildren().add(chessFigure.getRectangle());
        }
    }

    private void drawChessboard()
    {
        Color color = Color.NAVAJOWHITE;
        for (int row = 0; row < 8; row++)
        {
            for (int column = 0; column < 8; column++)
            {
                final ChessBoard.Tile tile = new ChessBoard.Tile(row + 1, column + 1, color);
                this.chessBoardGroup.getChildren().add(tile.getRectangle());
                this.chessBoard.getChessBoardTiles()[row][column] = tile;

                if (color == Color.NAVAJOWHITE)
                    color = Color.SADDLEBROWN;
                else
                    color = Color.NAVAJOWHITE;
            }
            if (color == Color.NAVAJOWHITE)
                color = Color.SADDLEBROWN;
            else
                color = Color.NAVAJOWHITE;
        }
    }

    public static void main( String[] args )
    {
        ChessGame.launch(args);
    }

    public Group getChessBoardView()
    {
        return this.chessBoardGroup;
    }

    public ChessBoard getChessBoard()
    {
        return this.chessBoard;
    }

    public List<ChessFigure> getAliveWhiteFigures()
    {
        return this.aliveWhiteFigures;
    }

    public List<ChessFigure> getAliveBlackFigures()
    {
        return this.aliveBlackFigures;
    }
}
