package regis.mido830;

public class Game implements Comparable<Game> {
    private final int appID;
    private final String name;
    private final String logoLink;
    private final String storeLink;
    private Double hoursPlayed;
    private int playerCount;

    public Game(int appID, String name, String logoLink, String storeLink, Double hoursPlayed, int playerCount) {
        this.appID = appID;
        this.name = name;
        this.logoLink = logoLink;
        this.storeLink = storeLink;
        this.hoursPlayed = hoursPlayed;
        this.playerCount = playerCount;
    }

    @Override
    public String toString() {
        return playerCount + " - " + name;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getAppID() {
        return appID;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public int compareTo(Game game) {//allowing to sort games by playerCount
        return Integer.compare(game.playerCount, this.playerCount);
    }
}
