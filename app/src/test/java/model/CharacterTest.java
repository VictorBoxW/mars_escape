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
        // Base defense of Player is 2. Attack of 10 should deal 10 - 2 = 8 damage.
        int damage = testCharacter.takeDamage(10);
        assertEquals(8, damage);
        assertEquals(92, testCharacter.getHealth());
    }

    @Test
    void testTakeDamageWithShield() {
        testCharacter.addShield(10);
        // Attack of 12 should deal 12 - 2 = 10 damage.
        // Shield should absorb all 10 damage and drop to 0.
        int damage = testCharacter.takeDamage(12);
        assertEquals(10, damage);
        assertEquals(100, testCharacter.getHealth());
        assertEquals(0, testCharacter.getShield());

        // Next attack of 10 should deal 10 - 2 = 8 damage total.
        // Shield is empty, so all 8 hits health directly.
        damage = testCharacter.takeDamage(10);
        assertEquals(8, damage);
        assertEquals(92, testCharacter.getHealth());
        assertEquals(0, testCharacter.getShield());
    }

    @Test
    void testHeal() {
        testCharacter.takeDamage(50);
        assertEquals(52, testCharacter.getHealth());

        int healed = testCharacter.heal(20);
        assertEquals(20, healed);
        assertEquals(72, testCharacter.getHealth());

        healed = testCharacter.heal(100); // Overheal
        assertEquals(28, healed);
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
        testCharacter.takeDamage(102); // 102 - 2 = 100 damage
        assertFalse(testCharacter.isAlive());
        assertEquals(0, testCharacter.getHealth());
    }
}
