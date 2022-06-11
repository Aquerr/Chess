package pl.bartlomiejstepien.chess.history;

import pl.bartlomiejstepien.chess.piece.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameHistory
{
    private List<HistoryLine> history = new ArrayList<>();

    public GameHistory()
    {

    }

    public void add(HistoryLine historyLine)
    {
        this.history.add(historyLine);
    }

    public HistoryLine getLastMove(Side side)
    {
        List<HistoryLine> lines = this.history.stream()
                .filter(historyLine -> historyLine.getSide().equals(side))
                .collect(Collectors.toList());
        return lines.isEmpty() ? null : lines.get(lines.size() - 1);
    }

    public List<HistoryLine> getHistory()
    {
        return history;
    }
}
