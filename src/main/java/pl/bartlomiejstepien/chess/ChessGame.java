package pl.bartlomiejstepien.chess;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pl.bartlomiejstepien.chess.localization.Localization;
import pl.bartlomiejstepien.chess.piece.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChessGame extends Application
{
    private static ChessGame INSTANCE;

    private final ChessBoard chessBoard;
    private King blackKing;
    private King whiteKing;

    private Stage stage;
    private Scene scene;
    private Group root;
    private Group chessBoardGroup;

    private Label labelCurrentMove;
    private Label labelTimer;

    private Timer timer;
    private int seconds;

    private final List<ChessPiece> aliveWhiteFigures = new ArrayList<>();
    private final List<ChessPiece> aliveBlackFigures = new ArrayList<>();

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

    public void switchSide()
    {
        isWhiteMove = !isWhiteMove;
        Platform.runLater(() -> labelCurrentMove.setText(Localization.translate("currentmove") + ": " + (this.isWhiteMove ? Localization.translate("whiteside") : Localization.translate("blackside"))));
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage = primaryStage;
        this.root = new Group();
        this.scene = new Scene(root, 600, 600);

        this.labelCurrentMove = new Label(Localization.translate("currentmove") + ": " + (this.isWhiteMove ? Localization.translate("whiteside") : Localization.translate("blackside")));
        this.labelCurrentMove.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        this.labelCurrentMove.setTranslateX(60);
        this.labelCurrentMove.setTranslateY(15);
        this.root.getChildren().add(this.labelCurrentMove);

        this.labelTimer = new Label(Localization.translate("time") + " 00:00:00");
        this.root.getChildren().add(this.labelTimer);
        this.labelTimer.setTranslateX(390);
        this.labelTimer.setTranslateY(15);
        this.labelTimer.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));

        this.timer = new Timer();
        final TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                seconds++;
                Platform.runLater(() -> labelTimer.setText(Localization.translate("time") + ": " + LocalTime.MIN.plusSeconds(seconds).format(DateTimeFormatter.ISO_LOCAL_TIME)));
            }
        };
        this.timer.scheduleAtFixedRate(timerTask, 0, 1000L);

        this.chessBoardGroup = new Group();
        root.getChildren().add(chessBoardGroup);
        chessBoardGroup.setTranslateX(60);
        chessBoardGroup.setTranslateY(60);

        // Draw chessboard
        drawChessboard();
        setupChessFigures();

        primaryStage.setTitle(Localization.translate("chess"));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(windowEvent ->
        {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void setupChessFigures()
    {
        // Pawns
        for (int i = 1; i <= 8; i++)
        {
            final Pawn blackPawn = new Pawn(Side.BLACK, new ChessboardPosition(2, i));
            final Pawn whitePawn = new Pawn(Side.WHITE, new ChessboardPosition(7, i));
        }

        // Kings
        this.blackKing = new King(Side.BLACK, new ChessboardPosition(1,5));
        this.whiteKing = new King(Side.WHITE, new ChessboardPosition(8,5));

        // Queens
        final Queen blackQueen = new Queen(Side.BLACK, new ChessboardPosition(1, 4));
        final Queen whiteQueen = new Queen(Side.WHITE, new ChessboardPosition(8, 4));

        // Rooks
        final Rook blackRook1 = new Rook(Side.BLACK, new ChessboardPosition(1, 1));
        final Rook blackRook2 = new Rook(Side.BLACK, new ChessboardPosition(1, 8));
        final Rook whiteRook1 = new Rook(Side.WHITE, new ChessboardPosition(8, 1));
        final Rook whiteRook2 = new Rook(Side.WHITE, new ChessboardPosition(8, 8));

        // Knights
        final Knight blackKnight1 = new Knight(Side.BLACK, new ChessboardPosition(1, 2));
        final Knight blackKnight2 = new Knight(Side.BLACK, new ChessboardPosition(1, 7));
        final Knight whiteKnight1 = new Knight(Side.WHITE, new ChessboardPosition(8, 2));
        final Knight whiteKnight2 = new Knight(Side.WHITE, new ChessboardPosition(8, 7));

        // Bishops
        final Bishop blackBishop1 = new Bishop(Side.BLACK, new ChessboardPosition(1, 3));
        final Bishop blackBishop2 = new Bishop(Side.BLACK, new ChessboardPosition(1, 6));
        final Bishop whiteBishop1 = new Bishop(Side.WHITE, new ChessboardPosition(8, 3));
        final Bishop whiteBishop2 = new Bishop(Side.WHITE, new ChessboardPosition(8, 6));

        // Add figure rectangles/boxes to view
        for (final ChessPiece chessPiece : this.aliveWhiteFigures)
        {
            this.chessBoardGroup.getChildren().add(chessPiece.getRectangle());
        }

        for (final ChessPiece chessPiece : this.aliveBlackFigures)
        {
            this.chessBoardGroup.getChildren().add(chessPiece.getRectangle());
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

        char letter = 'A';
        for (int row = 1; row <= 8; row++)
        {
            final Label label = new Label(String.valueOf(letter));
            label.setTranslateX(row * 60 - 30);
            label.setTranslateY(-20);
            this.chessBoardGroup.getChildren().add(label);
            letter++;
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

    public List<ChessPiece> getAliveWhiteFigures()
    {
        return this.aliveWhiteFigures;
    }

    public List<ChessPiece> getAliveBlackFigures()
    {
        return this.aliveBlackFigures;
    }

    public King getBlackKing()
    {
        return blackKing;
    }

    public King getWhiteKing()
    {
        return whiteKing;
    }
}
