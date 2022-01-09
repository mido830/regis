import org.junit.jupiter.api.Test;
import regis.mido830.GameList;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class GameListTest {

    GameList testList = new GameList();

    @Test
    public void checkPlayerCountTest() {
        assertTrue(testList.checkPlayerCount(730) > 0);
        //checks if the number of players for CS:GO (the most popular game on Steam) is greater than 0
    }
}
