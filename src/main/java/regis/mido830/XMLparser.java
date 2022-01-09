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
import java.util.Optional;

public class XMLparser {

    private File gamesFile;

    public File getGamesFile() {
        return gamesFile;
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

            NodeList gamesNodeList = gamesNode.getElementsByTagName("game");

            if (gamesNodeList.getLength() == 0) { //applies when the profile is public but the game list is not
                System.out.println("The profile is public but the game list is not.");
                return;
            }

            for (int i = 0; i < gamesNodeList.getLength(); i++) { //parses additional info about games in case you want to display something more than just the player count and the name
                Element game = (Element) gamesNodeList.item(i);
                int appID = Integer.parseInt(game.getElementsByTagName("appID").item(0).getTextContent());
                String gameName = game.getElementsByTagName("name").item(0).getTextContent();
                String logoLink = game.getElementsByTagName("logo").item(0).getTextContent();
                String storeLink = game.getElementsByTagName("storeLink").item(0).getTextContent();
                double hoursPlayed = Optional.ofNullable(game.getElementsByTagName("hoursOnRecord").item(0)).map(hp -> hp.getTextContent().replace(",", "")).map(Double::parseDouble).orElse(0.0);

                Game gameObject = new Game(appID, gameName, logoLink, storeLink, hoursPlayed, 0); //initially setting playerCount to 0 when creating regis.Game objects, it gets updated later
                gameList.add(gameObject);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Failed to parse the game list\n" + e.getMessage());
        }
    }
}
