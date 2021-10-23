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
import java.util.concurrent.atomic.AtomicInteger;

public class Logic {
static File gamesFile;

    public static int checkPlayerCount(int appID) { //checks the player count for a game by appID and returns in, this takes a lot of time
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
            e.printStackTrace();
        }
        return playersOnline;
    }

    public static void downloadXML(String profileLink) { //downloads games.xml (the list of owned games)
        try {
            gamesFile = File.createTempFile("games", ".xml", null);
            URL profile = new URL(profileLink);
            ReadableByteChannel rbc = Channels.newChannel(profile.openStream());
            FileOutputStream fos = new FileOutputStream(gamesFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateList(ArrayList<Game> list) { //updates the "playerCount" variable for each game. Progress counting also happens here.
        ArrayList<Thread> threads = new ArrayList<>();
        AtomicInteger progress = new AtomicInteger();

        for (int i = 0; i < list.size(); i++) {
            int finalI = i;

            Thread thread = new Thread(() -> {
                list.get(finalI).setPlayerCount(Logic.checkPlayerCount(list.get(finalI).getAppID()));
                progress.incrementAndGet();
                System.out.print("\rProgress: " + progress + "/" + list.size() + " games checked.");
            });

            thread.start();
            threads.add(thread);
        }

        for (Thread thr : threads) {
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n");
    }

    public static void excludeBelowX(ArrayList<Game> list, int x) {
        list.removeIf(game -> game.getPlayerCount() < x);
    }
}
