package pl.bartlomiejstepien.chess.online.packets;

import pl.bartlomiejstepien.chess.ChessboardPosition;

public class MovePacket implements Packet
{
    private ChessboardPosition chessPieceFromTile;
    private ChessboardPosition movedTo;

    public MovePacket()
    {

    }

    public MovePacket(ChessboardPosition chessPieceFromTile, ChessboardPosition movedTo)
    {
        this.chessPieceFromTile = chessPieceFromTile;
        this.movedTo = movedTo;
    }

    public ChessboardPosition getChessFromTile()
    {
        return chessPieceFromTile;
    }

    public ChessboardPosition getMovedTo()
    {
        return movedTo;
    }

    @Override
    public String toString()
    {
        return "MovePacket{" +
                "chessPieceFromTile=" + chessPieceFromTile +
                ", movedTo=" + movedTo +
                '}';
    }
}
