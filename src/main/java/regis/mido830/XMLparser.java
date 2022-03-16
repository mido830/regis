package regis.mido830;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class XMLparser {

    private File gamesFile;

    public File getGamesFile() {
        return gamesFile;
    }

    public boolean downloadXML(String profileID, String steamWebAPIkey) { //downloads games.xml (the list of owned games)
        try {
            gamesFile = File.createTempFile("games", ".xml", null);
            URL gamesList = new URL("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + steamWebAPIkey + "&steamid=" + profileID + "&include_played_free_games=1&include_appinfo=1&format=xml");
            ReadableByteChannel rbc = Channels.newChannel(gamesList.openStream());
            FileOutputStream fos = new FileOutputStream(gamesFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to download your game list. Make sure your Steam profile URL is correct.");
            return false;
        }
    }

    public void parseXML(List<Game> gameList, File gamesFile) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(gamesFile);
            doc.getDocumentElement().normalize();

            Element gamesNode = (Element) doc.getElementsByTagName("games").item(0);

            if (gamesNode == null) { //applies when the profile is private
                System.out.println("The profile is private or does not exist.");
                return;
            }

            NodeList gamesNodeList = gamesNode.getElementsByTagName("message");

            if (gamesNodeList.getLength() == 0) { //applies when the profile is public but the game list is not
                System.out.println("The profile is public but the game list is not.");
                return;
            }

            for (int i = 0; i < gamesNodeList.getLength(); i++) {
                Element game = (Element) gamesNodeList.item(i);
                int appID = Integer.parseInt(game.getElementsByTagName("appid").item(0).getTextContent());
                String gameName = game.getElementsByTagName("name").item(0).getTextContent();

                Game gameObject = new Game(appID, gameName, 0); //initially setting playerCount to 0 when creating Game objects, it gets updated later
                gameList.add(gameObject);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Failed to parse the game list\n" + e.getMessage());
        }
    }
}
