package pl.bartlomiejstepien.chess.online.packets;

public interface Packet
{
    PacketType packetType();

    enum PacketType
    {
        MOVE
    }
}
