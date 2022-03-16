package regis.mido830;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameList {

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

    public void updateList(List<Game> list) { //updates the "playerCount" variable for each game. Progress counting also happens here.
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads, Thread::new);

        AtomicInteger progress = new AtomicInteger();
        CompletableFuture<Void> future;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        System.out.println("\nDDoSing Steam API in progress... :)\n");

        for (Game game : list) {
            future = CompletableFuture.runAsync(() -> {
                game.setPlayerCount(checkPlayerCount(game.getAppID()));
                progress.incrementAndGet();
                System.out.printf("\rProgress: %s/%d games checked.", progress, list.size());
            }, executor);
            futures.add(future);
        }

        executor.shutdown();

        for (CompletableFuture<Void> i : futures) {
            try {
                i.get();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("\n");
    }

    public void excludeBelowX(int x) {
        list.removeIf(game -> game.getPlayerCount() < x);
    }
}
