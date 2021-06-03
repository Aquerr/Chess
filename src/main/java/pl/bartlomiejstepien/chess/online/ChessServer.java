package pl.bartlomiejstepien.chess.online;

import javafx.application.Platform;
import pl.bartlomiejstepien.chess.ChessBoard;
import pl.bartlomiejstepien.chess.ChessGame;
import pl.bartlomiejstepien.chess.ChessboardPosition;
import pl.bartlomiejstepien.chess.online.packets.MovePacket;
import pl.bartlomiejstepien.chess.piece.ChessPiece;
import pl.bartlomiejstepien.chess.piece.Side;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChessServer implements ChessOnlineConnection
{
    private static final int DEFAULT_PORT = 19_009;

    private final ServerSocket serverSocket;

    private ClientOnlineConnection connectedClient;

    public ChessServer() throws IOException
    {
        this.serverSocket = new ServerSocket(DEFAULT_PORT);
    }

    public void awaitForClient(Runnable onConnectRunnable)
    {
        Thread serverThread = new Thread(() ->{
            try
            {
                Socket socket = this.serverSocket.accept();
                connectedClient = ClientOnlineConnection.of(socket, this::processReceivedPacket);
                System.out.println("Client connected from: " + socket.getInetAddress());
                onConnectRunnable.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }

    public void sendMessage(MovePacket packet)
    {
        this.connectedClient.sendMessage(packet);
    }

    private void processReceivedPacket(MovePacket packet)
    {
        System.out.println("SERVER: Received packet from client: " + packet);
        Platform.runLater(() -> {
            ChessPiece chessPiece = ChessGame.getGame().getChessBoard().getFigureAt(packet.getChessFromTile().getRow(), packet.getChessFromTile().getColumn());
            chessPiece.moveTo(ChessGame.getGame().getChessBoard().getTileAt(packet.getMovedTo().getRow(), packet.getMovedTo().getColumn()));
        });
    }
}
