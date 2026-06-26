package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("MarsExplorer");
    }

    @Test
    void testInventoryManagement() {
        Item key = new Key(1);
        player.addItem(key);
        
        assertEquals(1, player.getInventory().size());
        assertTrue(player.getInventory().contains(key));
        
        player.removeItem(key);
        assertEquals(0, player.getInventory().size());
    }

    @Test
    void testMovement() {
        int initialX = player.getX();
        int initialY = player.getY();
        
        player.move(10, -20);
        assertEquals(initialX + 10, player.getX());
        assertEquals(initialY - 20, player.getY());
        
        player.setPosition(100, 200);
        assertEquals(100, player.getX());
        assertEquals(200, player.getY());
    }

    @Test
    void testRotationAndWalkPhase() {
        player.setRotation(1.5);
        assertEquals(1.5, player.getRotation());
        
        player.updateWalkPhase(0.5);
        assertEquals(0.5, player.getWalkPhase());
        
        player.updateWalkPhase(Math.PI * 2); // Should wrap around
        assertEquals(0.5, player.getWalkPhase(), 0.0001);
    }

}
