package pl.bartlomiejstepien.chess.online;

import pl.bartlomiejstepien.chess.online.packets.MovePacket;
import pl.bartlomiejstepien.chess.online.packets.Packet;

public interface ChessOnlineConnection
{
    void sendMessage(MovePacket packet);
}
