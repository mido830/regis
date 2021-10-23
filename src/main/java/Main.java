import java.util.Collections;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Enter the Steam profile URL:");
        Scanner scanner = new Scanner(System.in);
        String profileLink = scanner.nextLine();

        System.out.println("\nExclude games below X players. Enter X to filter the list or press enter to show all games:");
        int threshold;
        try {
            threshold = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            threshold = 0;
        }

        if (profileLink.endsWith("/")) {
            profileLink += "games?tab=all&xml=1";
        } else {
            profileLink += "/games?tab=all&xml=1";
        }

        System.out.println("\nDDoSing Steam API in progress... :)\n");
        Logic.downloadXML(profileLink);                 //downloads games.xml via Steam Web API
        GameList list = new GameList();                 //creates an empty list to store info about owned games
        XMLparser.parseXML(list.getList());             //parses games.xml and fills the list
        Logic.updateList(list.getList());               //downloads player count information for every game on the list and updates the list with this information
        Collections.sort(list.getList());               //sorts the list by player count
        Logic.excludeBelowX(list.getList(), threshold); //removes games from the list with playerCount below X
        System.out.println(list);

        Scanner exit = new Scanner(System.in);
        exit.nextLine();
    }
}
