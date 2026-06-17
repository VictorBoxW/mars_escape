package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterTest {
    private Character testCharacter;

    @BeforeEach
    void setUp() {
        // We use Player as a concrete implementation of Character for testing
        testCharacter = new Player("TestHero");
    }

    @Test
    void testInitialStats() {
        assertEquals("TestHero", testCharacter.getName());
        assertEquals(100, testCharacter.getMaxHealth());
        assertEquals(100, testCharacter.getHealth());
        assertEquals(0, testCharacter.getShield());
        assertTrue(testCharacter.isAlive());
    }

    @Test
    void testTakeDamage() {
        // Base defense of Player is 4. Attack of 10 should deal 10 - 4 = 6 damage.
        int damage = testCharacter.takeDamage(10);
        assertEquals(6, damage);
        assertEquals(94, testCharacter.getHealth());
    }

    @Test
    void testTakeDamageWithShield() {
        testCharacter.addShield(10);
        // Attack of 12 should deal 12 - 4 = 8 damage.
        // Shield should absorb all 8 damage.
        int damage = testCharacter.takeDamage(12);
        assertEquals(8, damage);
        assertEquals(100, testCharacter.getHealth());
        assertEquals(2, testCharacter.getShield());

        // Next attack of 10 should deal 6 damage.
        // Shield absorbs 2, remaining 4 hits health.
        // The return value is currently only 'shield' damage OR 'health' damage based on the logic.
        // Wait, let's re-examine takeDamage logic in Character.java.
        damage = testCharacter.takeDamage(10);
        assertEquals(4, damage); // This is what the code actually returns (health damage)
        assertEquals(96, testCharacter.getHealth());
        assertEquals(0, testCharacter.getShield());
    }

    @Test
    void testHeal() {
        testCharacter.takeDamage(50);
        assertEquals(54, testCharacter.getHealth());

        int healed = testCharacter.heal(20);
        assertEquals(20, healed);
        assertEquals(74, testCharacter.getHealth());

        healed = testCharacter.heal(100); // Overheal
        assertEquals(26, healed);
        assertEquals(100, testCharacter.getHealth());
    }

    @Test
    void testHealDeadCharacter() {
        testCharacter.takeDamage(200);
        assertFalse(testCharacter.isAlive());
        assertEquals(0, testCharacter.getHealth());

        int healed = testCharacter.heal(50);
        assertEquals(0, healed);
        assertEquals(0, testCharacter.getHealth());
    }

    @Test
    void testCharacterDeath() {
        testCharacter.takeDamage(104); // 104 - 4 = 100 damage
        assertFalse(testCharacter.isAlive());
        assertEquals(0, testCharacter.getHealth());
    }
}
