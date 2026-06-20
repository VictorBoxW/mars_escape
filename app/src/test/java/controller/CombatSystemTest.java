package controller;

import model.Enemy;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CombatSystemTest {
    private CombatSystem combatSystem;
    private Player player;
    private Enemy enemy;

    @BeforeEach
    void setUp() {
        // CombatSystem(Random) allows us to control the "random" behavior for deterministic testing
        // For simple attack/defend tests without dodge chance, default Random is fine,
        // but we can pass a mocked or seeded Random for specific dodge scenarios.
        combatSystem = new CombatSystem(new Random(42)); // Seeded for determinism
        player = new Player("Hero"); // HP: 100, ATK: 12, DEF: 2
        enemy = new Enemy("Alien", 36, 10, "I will eat you!"); // HP: 36, ATK: 10
    }

    @Test
    void testAttackTurn() {
        // Player attacks enemy: 12 ATK = 12 Damage. Enemy HP: 36 -> 24.
        // Enemy attacks player: 10 ATK - 2 DEF = 8 Damage. Player HP: 100 -> 92.
        CombatTurnResult result = combatSystem.attack(player, enemy);

        assertEquals(24, enemy.getHealth());
        assertEquals(92, player.getHealth());
        assertFalse(result.isEnemyDefeated());
        assertFalse(result.isPlayerDefeated());
        assertTrue(result.getMessage().contains("Hero attacks Alien for 12 damage"));
        assertTrue(result.getMessage().contains("Alien hits Hero for 8 damage"));
    }

    @Test
    void testDefeatEnemy() {
        // Enemy has 36 HP. 3 player attacks should kill it.
        combatSystem.attack(player, enemy);
        combatSystem.attack(player, enemy);
        CombatTurnResult result = combatSystem.attack(player, enemy);

        assertEquals(0, enemy.getHealth());
        assertFalse(enemy.isAlive());
        assertTrue(result.isEnemyDefeated());
        assertFalse(result.isPlayerDefeated());
        assertTrue(result.getMessage().contains("Alien is defeated"));
    }

    @Test
    void testDodgeSuccess() {
        // We use a custom Random to force a successful dodge (nextInt(100) < 60)
        CombatSystem dodgingCombatSystem = new CombatSystem(new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // Always successful dodge
            }
        });

        CombatTurnResult result = dodgingCombatSystem.dodge(player, enemy);
        assertEquals(100, player.getHealth()); // No damage taken
        assertTrue(result.getMessage().contains("Hero dodged Alien's attack"));
    }

    @Test
    void testDodgeFailure() {
        // We use a custom Random to force a failed dodge (nextInt(100) >= 60)
        CombatSystem dodgingCombatSystem = new CombatSystem(new Random() {
            @Override
            public int nextInt(int bound) {
                return 97; // Always failed dodge
            }
        });

        // Failed dodge: Enemy ATK is halved (10 / 2 = 5).
        // Damage taken: 5 ATK - 2 DEF = 3 Damage.
        CombatTurnResult result = dodgingCombatSystem.dodge(player, enemy);
        assertEquals(97, player.getHealth());
        assertTrue(result.getMessage().contains("Alien clips Hero for 3 damage"));
    }
}
