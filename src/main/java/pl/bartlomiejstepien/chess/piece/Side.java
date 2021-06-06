package pl.bartlomiejstepien.chess.piece;

public enum Side
{
    WHITE, BLACK;

    public Side opposite()
    {
        return this == BLACK ? WHITE : BLACK;
    }
}
