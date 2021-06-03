package pl.bartlomiejstepien.chess.online;

import com.google.gson.Gson;
import pl.bartlomiejstepien.chess.online.packets.MovePacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ClientOnlineConnection implements ChessOnlineConnection
{
    private static final int DEFAULT_PORT = 19_009;
    private static final Gson GSON = new Gson();

    private final Socket socket;
    private final BufferedReader inputStream;
    private final OutputStreamWriter outputStream;

    public static ClientOnlineConnection of(Socket socket, final Consumer<MovePacket> packetProcessor) throws Exception
    {
        return new ClientOnlineConnection(socket, socket.getInputStream(), socket.getOutputStream(), packetProcessor);
    }

    public static ClientOnlineConnection connect(String ipAddressWithPort, final Consumer<MovePacket> packetProcessor) throws Exception
    {
        ChessServerAddress chessServerAddress = ChessServerAddress.of(ipAddressWithPort);

        Socket socket = new Socket(chessServerAddress.getIpAddress(), chessServerAddress.getPort());

        return new ClientOnlineConnection(socket, socket.getInputStream(), socket.getOutputStream(), packetProcessor);
    }
    
    private ClientOnlineConnection(Socket socket, InputStream inputStream, OutputStream outputStream, final Consumer<MovePacket> packetProcessor)
    {
        this.socket = socket;
        this.inputStream = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.outputStream = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        processReceivedMessages(packetProcessor);

    }

    public void sendMessage(MovePacket packet)
    {
        try
        {
            String json = GSON.toJson(packet);
            System.out.println("Json to send: " + json);
            this.outputStream.write(json + System.lineSeparator());
            this.outputStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void processReceivedMessages(Consumer<MovePacket> consumer)
    {
        var thread = new Thread(() ->
        {
            while (true)
            {
                consumer.accept(awaitMessage());
            }
        });
        thread.start();
    }

    private MovePacket awaitMessage()
    {
        try
        {
            return GSON.fromJson(this.inputStream.readLine(), MovePacket.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static final class ChessServerAddress
    {
        private final String ipAddress;
        private final int port;

        public static ChessServerAddress of(String ipAddressWithPort)
        {
            String[] ipAndPort;
            if (ipAddressWithPort.contains(":"))
            {
                ipAndPort = ipAddressWithPort.split(":");
            }
            else
            {
                ipAndPort = new String[] {ipAddressWithPort, String.valueOf(DEFAULT_PORT)};
            }

            return new ChessServerAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        }

        private ChessServerAddress(final String ipAddress, int port)
        {
            this.ipAddress = ipAddress;
            this.port = port;
        }

        public String getIpAddress()
        {
            return ipAddress;
        }

        public int getPort()
        {
            return port;
        }
    }
}
