package pl.bartlomiejstepien.chess;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import pl.bartlomiejstepien.chess.localization.Localization;
import pl.bartlomiejstepien.chess.online.ChessOnlineConnection;
import pl.bartlomiejstepien.chess.online.ClientOnlineConnection;
import pl.bartlomiejstepien.chess.online.ChessServer;
import pl.bartlomiejstepien.chess.online.packets.MovePacket;
import pl.bartlomiejstepien.chess.piece.*;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class ChessGame extends Application
{
    private static final int WINDOW_HEIGHT = 600;
    private static final int WINDOW_WIDTH = 600;

    private static ChessGame INSTANCE;

    private ChessBoard chessBoard;

    private King blackKing;
    private King whiteKing;

    private Stage stage;
    private Scene scene;
    private VBox root;
    private Group mainGroup;
    private Group chessBoardGroup;

    private Label labelCurrentMove;
    private Label labelTimer;

    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int seconds;

    private final List<ChessPiece> aliveWhiteFigures = new LinkedList<>();
    private final List<ChessPiece> aliveBlackFigures = new LinkedList<>();

    private Side currentMoveSide = Side.WHITE;

    private MenuBar menuBar;

    // Online
    private ChessOnlineConnection chessOnlineConnection;

    public ChessGame()
    {
        INSTANCE = this;
    }

    public static ChessGame getGame()
    {
        return INSTANCE;
    }

    public Side getCurrentMoveSide()
    {
        return this.currentMoveSide;
    }

    public void switchSide()
    {
        this.currentMoveSide = this.currentMoveSide == Side.WHITE ? Side.BLACK : Side.WHITE;
        Platform.runLater(() -> labelCurrentMove.setText(Localization.translate("currentmove") + ": " + (this.currentMoveSide == Side.WHITE ? Localization.translate("whiteside") : Localization.translate("blackside"))));
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage = primaryStage;

        setupUI();

        primaryStage.setTitle(Localization.translate("chess"));
        primaryStage.setOnCloseRequest(windowEvent -> closeGame());
        primaryStage.show();
    }

    private void closeGame()
    {
        Platform.exit();
        System.exit(0);
    }

    private void setupMenuBar()
    {
        Menu gameMenu = new Menu(Localization.translate("menu.game"));

        Menu multiplayerMenuItem = new Menu(Localization.translate("menu.game.multiplayer"));
        gameMenu.getItems().add(multiplayerMenuItem);

        MenuItem hostGame = new MenuItem(Localization.translate("menu.game.multiplayer.host_game"));
        hostGame.setOnAction(actionEvent -> hostGame());
        MenuItem connectToIp = new MenuItem(Localization.translate("menu.game.multiplayer.connect_to_ip"));
        connectToIp.setOnAction(actionEvent -> showConnectToIpPopup());
        multiplayerMenuItem.getItems().addAll(hostGame, connectToIp);

        MenuItem exit = new MenuItem(Localization.translate("menu.exit"));
        exit.setOnAction(actionEvent -> closeGame());
        gameMenu.getItems().add(exit);

        this.menuBar = new MenuBar();
        this.menuBar.getMenus().add(gameMenu);

        this.menuBar.setPrefWidth(WINDOW_WIDTH);

        this.root.getChildren().add(this.menuBar);
    }

    private void showConnectToIpPopup()
    {
        // Show modal window where user can enter IP address of the host

        System.out.println("Connecting to ip...");

        Popup popup = new Popup();

        VBox vBox = new VBox();
        vBox.setStyle("-fx-border-width: 1px");
        vBox.setStyle("-fx-border-color: black");
        vBox.setBackground(new Background(new BackgroundFill(Color.NAVAJOWHITE, null, null)));

        TextField ipAddressField = new TextField();
        ipAddressField.setOnAction(actionEvent -> {
            popup.hide();
            connectToIp(ipAddressField.getText());
        });

        Label label = new Label(Localization.translate("online.enter_ip_address") + ":");
        label.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        vBox.getChildren().add(label);
        vBox.getChildren().add(ipAddressField);

        popup.getContent().add(vBox);
        popup.show(this.stage);
    }

    private void connectToIp(String ipAddress)
    {
        System.out.println("Chosen IP Address: " + ipAddress);

        try
        {
            this.chessOnlineConnection = ClientOnlineConnection.connect(ipAddress, (packet) -> {
                System.out.println("CLIENT: Received packet from server: " + packet);
                if (packet instanceof MovePacket)
                {
                    MovePacket movePacket = (MovePacket) packet;
                    Platform.runLater(() -> {
                        ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(movePacket.getChessFromTile().getRow(), movePacket.getChessFromTile().getColumn());
                        chessPiece.moveTo(ChessGame.getGame().getChessBoard().getTileAt(movePacket.getMovedTo().getRow(), movePacket.getMovedTo().getColumn()));
                    });
                }
            });
            restartGame();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    private void hostGame()
    {
        System.out.println("Hosting game...");

        try
        {
            this.chessOnlineConnection = new ChessServer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("Chess server started!");

        Popup popup = showWaitForClientPopup();
        ((ChessServer)this.chessOnlineConnection).awaitForClient(() -> {
            Platform.runLater(() ->{
                root.setDisable(false);
                popup.hide();
                System.out.println("Hiding popup!");
                restartGame();
            });
        });
        System.out.println("Waiting for client to connect...");
    }

    private Popup showWaitForClientPopup()
    {
        this.root.setDisable(true);
        Popup popup = new Popup();

        VBox vBox = new VBox();
        vBox.setStyle("-fx-border-width: 1px");
        vBox.setStyle("-fx-border-color: black");
        vBox.setBackground(new Background(new BackgroundFill(Color.NAVAJOWHITE, null, null)));

        Label label = new Label(Localization.translate("online.waiting_for_player"));
        label.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        vBox.getChildren().add(label);

        popup.getContent().add(vBox);
        popup.setHideOnEscape(false);
        popup.show(this.stage);
        return popup;
    }

    private void setupChessFigures()
    {
        this.aliveWhiteFigures.clear();
        this.aliveBlackFigures.clear();

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
    }

    private void drawChessboard()
    {
        this.chessBoardGroup = new Group();
        mainGroup.getChildren().add(chessBoardGroup);
        chessBoardGroup.setTranslateX(60);
        chessBoardGroup.setTranslateY(60);

        Function<Color, Color> colorChanger = (color) -> color == Color.NAVAJOWHITE ? Color.SADDLEBROWN : Color.NAVAJOWHITE;
        Color color = Color.SADDLEBROWN;

        char letter = 'A';
        for (int row = 1; row <= 9; row++)
        {
            for (int column = 1; column <= 9; column++)
            {
                // Letters
                if (row == 1 && column == 1)
                {
                    continue;
                }
                else if (row == 1)
                {
                    final Label label = new Label(String.valueOf(letter));
                    label.setTranslateX((column - 1) * ChessBoard.TILE_SIZE - ChessBoard.TILE_SIZE / 2);
                    label.setTranslateY(-20);
                    this.chessBoardGroup.getChildren().add(label);
                    letter++;
                }
                // Numbers
                else if (column == 1)
                {
                    final Label label = new Label(String.valueOf(10 - row));
                    label.setTranslateY((row - 1) * ChessBoard.TILE_SIZE - ChessBoard.TILE_SIZE / 2);
                    label.setTranslateX(-20);
                    this.chessBoardGroup.getChildren().add(label);
                }
                // Actual Tiles
                else
                {
                    final ChessBoard.Tile tile = new ChessBoard.Tile(String.valueOf(letter) + (10 - row), row - 1, column - 1, color);
                    this.chessBoardGroup.getChildren().add(tile.getRectangle());
                    this.chessBoard.getChessBoardTiles()[row - 2][column - 2] = tile;

                    color = colorChanger.apply(color);
                }
            }
            color = colorChanger.apply(color);
        }
    }

    public static void startChess(String[] args)
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

    public List<ChessPiece> getAliveFigures(Side side)
    {
        return side == Side.BLACK ? getAliveBlackFigures() : getAliveWhiteFigures();
    }

    public void destroyPiece(final ChessPiece chessPiece)
    {
        if (chessPiece != null)
        {
            if (chessPiece.getSide() == Side.BLACK)
                getAliveBlackFigures().remove(chessPiece);
            else
                getAliveWhiteFigures().remove(chessPiece);
            getChessBoardView().getChildren().remove(chessPiece.getRectangle());
        }
    }

    public King getKing(Side side)
    {
        return side == Side.BLACK ? this.blackKing : this.whiteKing;
    }

    public void showPawnReplacementWindow(Side side, final ChessBoard.Tile tile)
    {
        this.root.setDisable(true);
        Popup popup = new Popup();

        VBox vBox = new VBox();
        vBox.setStyle("-fx-border-width: 1px");
        vBox.setStyle("-fx-border-color: black");
        vBox.setBackground(new Background(new BackgroundFill(Color.NAVAJOWHITE, null, null)));

        TilePane group = new TilePane(Orientation.HORIZONTAL, 0, 0.5);

        Label label = new Label(Localization.translate("select_chess_piece") + ":");
        label.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        vBox.getChildren().add(label);
        vBox.getChildren().add(group);

        final Rectangle queen = new Rectangle(50, 50);
        final Rectangle rook = new Rectangle(50, 50);
        final Rectangle bishop = new Rectangle(50, 50);
        final Rectangle knight = new Rectangle(50, 50);

        if (side == Side.BLACK)
        {
            queen.setFill(new ImagePattern(new Image("icons/icons8-queen-50.png")));
            rook.setFill(new ImagePattern(new Image("icons/icons8-rook-50.png")));
            bishop.setFill(new ImagePattern(new Image("icons/icons8-bishop-50.png")));
            knight.setFill(new ImagePattern(new Image("icons/icons8-knight-50.png")));
        }
        else
        {
            queen.setFill(new ImagePattern(new Image("icons/icons8-queen-50-white.png")));
            rook.setFill(new ImagePattern(new Image("icons/icons8-rook-50-white.png")));
            bishop.setFill(new ImagePattern(new Image("icons/icons8-bishop-50-white.png")));
            knight.setFill(new ImagePattern(new Image("icons/icons8-knight-50-white.png")));
        }

        queen.setStrokeType(StrokeType.CENTERED);
        rook.setStrokeType(StrokeType.CENTERED);
        bishop.setStrokeType(StrokeType.CENTERED);
        knight.setStrokeType(StrokeType.CENTERED);

        queen.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(queen, true));
        queen.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(queen, false));
        rook.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(rook, true));
        rook.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(rook, false));
        bishop.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(bishop, true));
        bishop.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(bishop, false));
        knight.addEventHandler(MouseEvent.MOUSE_ENTERED, new ChessBoard.HighlightTileEventHandler(knight, true));
        knight.addEventHandler(MouseEvent.MOUSE_EXITED, new ChessBoard.HighlightTileEventHandler(knight, false));

        queen.setOnMousePressed(mouseEvent ->
        {
            System.out.println(side.name() + " converted pawn to: Queen");
            destroyPiece(tile.getChessPiece());
            new Queen(side, new ChessboardPosition(tile.getRow(), tile.getColumn()));
            popup.hide();
            this.root.setDisable(false);
        });
        rook.setOnMousePressed(mouseEvent ->
        {
            System.out.println(side.name() + " converted pawn to: Rook");
            destroyPiece(tile.getChessPiece());
            new Rook(side, new ChessboardPosition(tile.getRow(), tile.getColumn()));
            popup.hide();
            this.root.setDisable(false);
        });
        bishop.setOnMousePressed(mouseEvent ->
        {
            System.out.println(side.name() + " converted pawn to: Bishop");
            destroyPiece(tile.getChessPiece());
            new Bishop(side, new ChessboardPosition(tile.getRow(), tile.getColumn()));
            popup.hide();
            this.root.setDisable(false);
        });
        knight.setOnMousePressed(mouseEvent ->
        {
            System.out.println(side.name() + " converted pawn to: Knight");
            destroyPiece(tile.getChessPiece());
            new Knight(side, new ChessboardPosition(tile.getRow(), tile.getColumn()));
            popup.hide();
            this.root.setDisable(false);
        });

        final ObservableList<Node> groupNodes = group.getChildren();
        groupNodes.add(queen);
        groupNodes.add(rook);
        groupNodes.add(bishop);
        groupNodes.add(knight);

        popup.getContent().add(vBox);

        popup.show(this.stage);
    }

    public boolean isOnline()
    {
        return this.chessOnlineConnection != null;
    }

    public ChessOnlineConnection getOnlineConnection()
    {
        return this.chessOnlineConnection;
    }

    public void restartGame()
    {
        this.chessBoard = new ChessBoard();
        this.mainGroup.getChildren().remove(this.chessBoardGroup);
        drawChessboard();
        setupChessFigures();
        restartTimer();
    }

    private void setupUI()
    {
        this.root = new VBox();
        this.scene = new Scene(this.root, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.mainGroup = new Group();
        this.mainGroup.prefWidth(WINDOW_WIDTH);
        this.mainGroup.prefHeight(WINDOW_HEIGHT);

        setupMenuBar();

        this.labelCurrentMove = new Label(Localization.translate("currentmove") + ": " + (this.currentMoveSide == Side.WHITE ? Localization.translate("whiteside") : Localization.translate("blackside")));
        this.labelCurrentMove.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));
        this.labelCurrentMove.setTranslateX(60);
        this.labelCurrentMove.setTranslateY(15);
        this.mainGroup.getChildren().add(this.labelCurrentMove);

        this.labelTimer = new Label(Localization.translate("time") + " 00:00:00");
        this.mainGroup.getChildren().add(this.labelTimer);
        this.labelTimer.setTranslateX(390);
        this.labelTimer.setTranslateY(15);
        this.labelTimer.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.REGULAR, 20));

        this.root.getChildren().add(this.mainGroup);

        VBox.setMargin(this.mainGroup, new Insets(0, 0, 0, 60));

        // Draw chessboard
        restartGame();

        this.stage.setScene(this.scene);
    }

    private void restartTimer()
    {
        if (this.timerTask != null)
            this.timerTask.cancel();

        this.seconds = 0;
        this.timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                seconds++;
                Platform.runLater(() -> labelTimer.setText(Localization.translate("time") + ": " + LocalTime.MIN.plusSeconds(seconds).format(DateTimeFormatter.ISO_LOCAL_TIME)));
            }
        };
        this.timer.scheduleAtFixedRate(timerTask, 0, 1000L);
    }
}
