package pl.bartlomiejstepien.chess.online;

import pl.bartlomiejstepien.chess.online.packets.Packet;
import pl.bartlomiejstepien.chess.piece.Side;

public interface ChessOnlineConnection
{
    void sendMessage(Packet packet);

    Side getChessSide();
}
