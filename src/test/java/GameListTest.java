import org.junit.jupiter.api.Test;
import regis.mido830.GameList;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class GameListTest {

    GameList testList = new GameList();

    @Test
    public void checkPlayerCountTest() {
        int CSGOid = 730;
        assertTrue(testList.checkPlayerCount(CSGOid) > 0);
        //CS:GO is the most popular game on Steam, so it should always have more than 0 players online.
    }
}
