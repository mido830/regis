package regis.mido830;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Scanner;
import java.util.prefs.Preferences;

public class Main {
    static Preferences userPreferences = Preferences.userNodeForPackage(Main.class);
    static XMLparser parser = new XMLparser();

    public static void main(String[] args) {
        GameList list = new GameList();     //creates an empty list to store info about owned games
        int threshold;
        String steamWebAPIkey = userPreferences.get("APIkey", "");
        if (steamWebAPIkey.equals("")) {
            steamWebAPIkey = getAPIkey();
        }

        String profileID = userPreferences.get("profileID", "");
        if (profileID.equals("")) {
            profileID = getProfileIDandDownloadXML(steamWebAPIkey, profileID);
        }

        parser.downloadXML(profileID, steamWebAPIkey);
        threshold = readThreshold(steamWebAPIkey, profileID);
        parser.parseXML(list.getList(), parser.getGamesFile());  //parses games.xml and fills the list
        parser.getGamesFile().deleteOnExit();                   //deletes games.xml when it's no longer needed
        list.updateList(list.getList());                        //asks Steam Web API for player count for each game on the list and updates the list with this information
        Collections.sort(list.getList());                       //sorts the list by player count
        list.excludeBelowX(threshold);                          //removes games from the list with playerCount below X
        System.out.println(list);

        System.out.println("\nPress Enter to exit.");
        Scanner exit = new Scanner(System.in);
        exit.nextLine();
        exit.close();
    }

    public static String getProfileIDandDownloadXML(String steamWebAPIkey, String profileID) {
        Scanner scanner = new Scanner(System.in);
        boolean successfullyDownloadedXML = false;
        String profileLink;

        while (!successfullyDownloadedXML) {
            System.out.println("Enter a Steam profile URL:");
            profileLink = scanner.nextLine();
            profileLink = profileLink.replaceAll(".*steamcommunity", "https://steamcommunity");
            boolean customURLcheck = profileLink.contains("/id/");     //there are 2 versions or steam profile URLs, custom and default (with steamID inside them). Only the latter one has "/profiles/" in the URL
            if (customURLcheck) {
                profileID = fetchProfileIdUsingCustomURL(profileLink, steamWebAPIkey);
            } else {
                profileID = profileLink.substring(36);
            }
            successfullyDownloadedXML = parser.downloadXML(profileID, steamWebAPIkey);
        }
        userPreferences.put("profileID", profileID);
        return profileID;
    }

    private static String getAPIkey() {
        Scanner scanner = new Scanner(System.in);
        boolean enteredCorrectAPIkey = false;
        String steamWebAPIkey = "";
        while (!enteredCorrectAPIkey) {
            System.out.println("Enter a Steam Web API key. You can generate one here: https://steamcommunity.com/dev/apikey");
            steamWebAPIkey = scanner.nextLine();
            enteredCorrectAPIkey = checkIfEnteredCorrectAPIkey(steamWebAPIkey);
        }
        userPreferences.put("APIkey", steamWebAPIkey);
        return steamWebAPIkey;
    }

    private static boolean checkIfEnteredCorrectAPIkey(String APIkey) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + APIkey + "&vanityurl=gaben"))
                .GET()
                .build();

        int success = 0;

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawResponse = response.body();

            JsonValue parsedResponse = Json.parse(rawResponse).asObject().get("response");
            success = parsedResponse.asObject().getInt("success", 0);

        } catch (IOException | InterruptedException | ParseException e) {
            System.out.println("Entered incorrect API key.");
        }
        return success == 1;
    }

    private static String fetchProfileIdUsingCustomURL(String customURL, String steamWebAPIkey) {
        customURL = customURL.substring(30);
        if (customURL.endsWith("/")) {
            customURL = customURL.substring(0, customURL.length() - 1);
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + steamWebAPIkey + "&vanityurl=" + customURL))
                .GET()
                .build();

        String profileID = "";

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawResponse = response.body();

            JsonValue parsedResponse = Json.parse(rawResponse).asObject().get("response");
            profileID = parsedResponse.asObject().getString("steamid", "");

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return profileID;
    }

    private static int readThreshold(String steamWebAPIkey, String profileID) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Exclude games below X players. Enter X to filter the list or press enter to show all games:");
        String input = scanner.nextLine();
        try {
            if ("wipe".equals(input) || "reset".equals(input)) {
                wipe(userPreferences);
                getAPIkey();
                getProfileIDandDownloadXML(steamWebAPIkey, profileID);
                input = String.valueOf(readThreshold(steamWebAPIkey, profileID));
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static void wipe(Preferences userPreferences) {
        userPreferences.remove("profileID");
        userPreferences.remove("APIkey");
        System.out.println("Settings wiped.");
    }
}
