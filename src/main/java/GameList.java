import java.util.ArrayList;

public class GameList {
    public ArrayList<Game> list;

    public GameList() {
        this.list = new ArrayList<>();
    }

    public ArrayList<Game> getList() {
        return list;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Game game : list) {
            result.append(game.toString()).append("\n");
        }
        return result.toString();
    }
}
