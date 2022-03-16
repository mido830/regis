import org.junit.jupiter.api.Test;
import regis.mido830.Game;
import regis.mido830.GameList;
import regis.mido830.XMLparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class XMLparserTest {

    @Test
    public void parseXMLtest() {
        //given
        GameList testList = new GameList();
        XMLparser parser = new XMLparser();
        URL url = this.getClass().getResource("gamesTest.xml");
        File gamesFileTest = new File(Objects.requireNonNull(url).getPath());   //mockup games.xml file

        //when
        parser.parseXML(testList.getList(), gamesFileTest);

        //then
        Game firstGameOnList = testList.getList().get(0);
        Game lastGameOnList = testList.getList().get(testList.getList().size() - 1);

        assertEquals(firstGameOnList.getAppID(), 10);
        assertEquals(firstGameOnList.getName(), "Counter-Strike");

        assertEquals(lastGameOnList.getAppID(), 46550);
        assertEquals(lastGameOnList.getName(), "Post Mortem");
    }

    @Test
    public void updateListTest() {
        //given
        GameList testList = new GameList();
        XMLparser parser = new XMLparser();
        URL url = this.getClass().getResource("gamesTest.xml");
        File gamesFileTest = new File(Objects.requireNonNull(url).getPath());   //mockup games.xml file

        //when
        parser.parseXML(testList.getList(), gamesFileTest);
        testList.updateList(testList.getList());

        //then
        assertTrue(testList.getList().get(0).getPlayerCount() > 0);
    }
}
