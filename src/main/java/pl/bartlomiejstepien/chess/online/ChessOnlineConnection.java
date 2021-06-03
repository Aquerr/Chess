package pl.bartlomiejstepien.chess.online;

import pl.bartlomiejstepien.chess.online.packets.Packet;

public interface ChessOnlineConnection
{
    void sendMessage(Packet packet);
}
