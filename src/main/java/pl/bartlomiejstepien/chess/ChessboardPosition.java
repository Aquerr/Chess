package pl.bartlomiejstepien.chess;

import java.util.Objects;

public class ChessboardPosition
{
    private final int row;
    private final int column;

    public static ChessboardPosition from(final int row, final int column)
    {
        return new ChessboardPosition(row, column);
    }

    public ChessboardPosition(final int row, final int column)
    {
        this.row = row;
        this.column = column;
    }

    public int getRow()
    {
        return this.row;
    }

    public int getColumn()
    {
        return this.column;
    }

    public ChessboardPosition addX(final int x)
    {
        return new ChessboardPosition(this.row + x, this.column);
    }

    public ChessboardPosition addY(final int y)
    {
        return new ChessboardPosition(this.row, this.column + y);
    }

    public ChessboardPosition minusX(final int x)
    {
        return new ChessboardPosition(this.row - x, this.column);
    }

    public ChessboardPosition minusY(final int y)
    {
        return new ChessboardPosition(this.row, this.column - y);
    }

    @Override
    public String toString()
    {
        return "ChessboardPosition{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessboardPosition position = (ChessboardPosition) o;
        return row == position.row &&
                column == position.column;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(row, column);
    }
}
