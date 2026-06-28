package persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.Castle;
import model.Consumable;
import model.Enemy;
import model.Floor;
import model.GameState;
import model.Player;
import model.Room;
import model.ShieldItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenceTest {

    @Test
    void testSaveAndLoadGameState(@TempDir File tempDir) throws Exception {
        // 1. Create a mock game state
        Player player = new Player("Test Astronaut");
        player.heal(20);
        player.addShield(50);
        player.setPosition(120, 240);
        player.setRotation(180.0);
        player.updateWalkPhase(0.5);

        Consumable medKit = new Consumable("Test Med Kit", "Heals 10", 10);
        ShieldItem shieldCell = new ShieldItem("Test Shield Cell", "Shield 20", 20);
        player.addItem(medKit);
        player.addItem(shieldCell);

        List<Rectangle> walls = List.of(new Rectangle(0, 0, 100, 10));
        List<Room> rooms = List.of(new Room("Test Room",
            List.of(new Enemy("Martian Scout", 30, 5,  "Taunt")),
            List.of(), 50, 50, 100, 100));
        Floor floor = new Floor("Test Floor", rooms, walls, new ArrayList<>(), 1000, 1000);
        
        Castle castle = new Castle(List.of(floor));
        Enemy activeEnemy = new Enemy("Martian Boss", 100, 15, "Fear me!", true);
        String logHistory = "Mars Escape\nInitial Log\nEvent occurred.";

        GameState originalState = new GameState(player, castle, activeEnemy, false, logHistory);

        // 2. Save using FilePersistenceManager
        File saveFile = new File(tempDir, "testgame.dat");
        PersistenceManager pm = new FilePersistenceManager();
        pm.save(originalState, saveFile.getAbsolutePath());

        // 3. Load using FilePersistenceManager
        GameState loadedState = pm.load(saveFile.getAbsolutePath());

        // 4. Assert values match
        assertNotNull(loadedState);
        assertFalse(loadedState.isGameOver());
        assertEquals("Mars Escape\nInitial Log\nEvent occurred.", loadedState.getLogHistory());

        // Player assertions
        Player loadedPlayer = loadedState.getPlayer();
        assertNotNull(loadedPlayer);
        assertEquals("Test Astronaut", loadedPlayer.getName());
        assertEquals(120, loadedPlayer.getX());
        assertEquals(240, loadedPlayer.getY());
        assertEquals(180.0, loadedPlayer.getRotation());
        assertEquals(0.5, loadedPlayer.getWalkPhase());
        assertEquals(50, loadedPlayer.getShield());
        assertEquals(100, loadedPlayer.getHealth());
        assertEquals(2, loadedPlayer.getInventory().size());
        assertEquals("Test Med Kit", loadedPlayer.getInventory().get(0).getName());
        assertEquals("Test Shield Cell", loadedPlayer.getInventory().get(1).getName());

        // Castle assertions
        Castle loadedCastle = loadedState.getCastle();
        assertNotNull(loadedCastle);
        assertEquals(1, loadedCastle.getFloorCount());
        assertEquals("Test Floor", loadedCastle.getCurrentFloor().getName());
        assertEquals(1, loadedCastle.getCurrentFloor().getRooms().size());
        assertEquals("Test Room", loadedCastle.getCurrentFloor().getRooms().get(0).getName());

        // Active enemy assertions
        Enemy loadedEnemy = loadedState.getCurrentEnemy();
        assertNotNull(loadedEnemy);
        assertEquals("Martian Boss", loadedEnemy.getName());
        assertEquals("Fear me!", loadedEnemy.getTaunt());
        assertEquals(100, loadedEnemy.getMaxHealth());
    }
}
