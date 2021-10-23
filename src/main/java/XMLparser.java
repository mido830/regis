import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class XMLparser {


    public static void parseXML(ArrayList<Game> gameList) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(Logic.gamesFile);
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
                double hoursPlayed;
                if (game.getElementsByTagName("hoursOnRecord").item(0) == null) {
                    hoursPlayed = 0.0;
                } else {
                    hoursPlayed = Double.parseDouble(game.getElementsByTagName("hoursOnRecord").item(0).getTextContent().replace(",", ""));
                }

                Game gameObject = new Game(appID, gameName, logoLink, storeLink, hoursPlayed, 0); //initially setting playerCount to 0 when creating Game objects, it gets updated later
                gameList.add(gameObject);
            }

            Logic.gamesFile.deleteOnExit();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
