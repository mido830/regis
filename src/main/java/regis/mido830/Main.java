package regis.mido830;

import java.util.Collections;
import java.util.Scanner;
import java.util.prefs.Preferences;

public class Main {
    static Preferences userPreferences = Preferences.userNodeForPackage(Main.class);

    public static void main(String[] args) {
        GameList list = new GameList();     //creates an empty list to store info about owned games
        int threshold;

        String profileLink = userPreferences.get("SteamProfileURL", "");
        if (profileLink.equals("")) {
            getProfileLinkAndDownloadXML(list);
        }

        list.downloadXML(profileLink);
        threshold = readThreshold(list);

        XMLparser.parseXML(list.getList()); //parses games.xml and fills the list
        list.updateList();                  //asks Steam Web API for player count for each game on the list and updates the list with this information
        Collections.sort(list.getList());   //sorts the list by player count
        list.excludeBelowX(threshold);      //removes games from the list with playerCount below X
        System.out.println(list);
    }

    public static void getProfileLinkAndDownloadXML(GameList list) {
        Scanner scanner = new Scanner(System.in);
        boolean successfullyDownloadedXML = false;
        String profileLink = "";
        while (!successfullyDownloadedXML) {
            System.out.println("Enter the Steam profile URL:");
            profileLink = scanner.nextLine();
            profileLink = profileLink.replaceAll(".*steamcommunity", "https://steamcommunity");
            profileLink = String.format("%s" + (profileLink.endsWith("/") ? "games?tab=all&xml=1" : "/games?tab=all&xml=1"), profileLink);
            successfullyDownloadedXML = list.downloadXML(profileLink);
        }
        userPreferences.put("SteamProfileURL", profileLink);
    }

    private static int readThreshold(GameList list) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nExclude games below X players. Enter X to filter the list or press enter to show all games:");
        String input = scanner.nextLine();
        try {
            if ("wipe".equals(input) || "reset".equals(input)) {
                wipe(userPreferences);
                getProfileLinkAndDownloadXML(list);
            }
            scanner.close();
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            scanner.close();
            return 0;
        }
    }

    public static void wipe(Preferences userPreferences) {
        userPreferences.remove("SteamProfileURL");
        System.out.println("Steam profile URL wiped.");
    }
}
