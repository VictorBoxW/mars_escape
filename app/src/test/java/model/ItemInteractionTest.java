package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class  ItemInteractionTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Hero");
    }

    @Test
    void testConsumableInteraction() {
        player.takeDamage(50); // HP: 52
        Consumable medKit = new Consumable("MedKit", "Heals 30", 30);
        player.addItem(medKit);
        
        String result = player.useItem(0);
        assertTrue(result.contains("used MedKit and recovered 30 health"));
        assertEquals(82, player.getHealth());
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void testShieldItemInteraction() {
        ShieldItem shield = new ShieldItem("Energy Shield", "Gains 40 shield", 40);
        player.addItem(shield);
        
        String result = player.useItem(0);
        assertTrue(result.contains("activated Energy Shield and gained 40 shield"));
        assertEquals(40, player.getShield());
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void testOverhealConsumable() {
        player.takeDamage(10); // HP: 92
        Consumable bigMedKit = new Consumable("MegaKit", "Heals 100", 100);
        player.addItem(bigMedKit);
        
        String result = player.useItem(0);
        assertTrue(result.contains("recovered 8 health"));
        assertEquals(100, player.getHealth());
    }

    @Test
    void testNoUsableItem() {
        String result = player.useItem(0);
        assertEquals("No usable item selected.", result);
    }
}
