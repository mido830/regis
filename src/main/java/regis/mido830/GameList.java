package regis.mido830;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameList {

    static File gamesFile;
    private List<Game> list;

    public GameList() {
        this.list = new ArrayList<>();
    }

    public List<Game> getList() {
        return list;
    }

    @Override
    public String toString() {
        return list.stream().map(String::valueOf).collect(Collectors.joining("\n"));
    }

    public int checkPlayerCount(int appID) { //checks the player count for a game by appID and returns in, this takes a lot of time
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=" + appID + "&format=json"))
                .GET()
                .build();

        int playersOnline = 0;

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawResponse = response.body();

            JsonValue parsedResponse = Json.parse(rawResponse).asObject().get("response");
            playersOnline = parsedResponse.asObject().getInt("player_count", 0);

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return playersOnline;
    }

    public boolean downloadXML(String profileLink) { //downloads games.xml (the list of owned games)
        try {
            gamesFile = File.createTempFile("games", ".xml", null);
            URL profile = new URL(profileLink);
            ReadableByteChannel rbc = Channels.newChannel(profile.openStream());
            FileOutputStream fos = new FileOutputStream(gamesFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to download your game list. Make sure your Steam profile URL is correct.");
            return false;
        }
    }

    public void updateList() { //updates the "playerCount" variable for each game. Progress counting also happens here.
        AtomicInteger progress = new AtomicInteger();

        System.out.println("\nDDoSing Steam API in progress... :)\n");

        list.parallelStream()
                .map(game ->
                        CompletableFuture.supplyAsync(() -> checkPlayerCount(game.getAppID()))
                                .thenApply(count -> {
                                    System.out.println(String.format("Found: %s for game id: %s", count, game.getAppID()));
                                    game.setPlayerCount(count);
                                    return game;
                                }))
                .forEach(CompletableFuture::join);
        System.out.println(System.lineSeparator());
    }

    public void excludeBelowX(int x) {
        list.removeIf(game -> game.getPlayerCount() < x);
    }
}
