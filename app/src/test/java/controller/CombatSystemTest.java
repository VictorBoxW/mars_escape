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
        player = new Player("Hero"); // HP: 100, ATK: 12, DEF: 4
        enemy = new Enemy("Alien", 30, 10, 2, "I will eat you!"); // HP: 30, ATK: 10, DEF: 2
    }

    @Test
    void testAttackTurn() {
        // Player attacks enemy: 12 ATK - 2 DEF = 10 Damage. Enemy HP: 30 -> 20.
        // Enemy attacks player: 10 ATK - 4 DEF = 6 Damage. Player HP: 100 -> 94.
        CombatTurnResult result = combatSystem.attack(player, enemy);

        assertEquals(20, enemy.getHealth());
        assertEquals(94, player.getHealth());
        assertFalse(result.isEnemyDefeated());
        assertFalse(result.isPlayerDefeated());
        assertTrue(result.getMessage().contains("Hero attacks Alien for 10 damage"));
        assertTrue(result.getMessage().contains("Alien hits Hero for 6 damage"));
    }

    @Test
    void testDefeatEnemy() {
        // Enemy has 30 HP. 3 player attacks should kill it.
        combatSystem.attack(player, enemy);
        combatSystem.attack(player, enemy);
        CombatTurnResult result = combatSystem.attack(player, enemy);

        assertEquals(0, enemy.getHealth());
        assertFalse(enemy.isAlive());
        assertTrue(result.isEnemyDefeated());
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
                return 99; // Always failed dodge
            }
        });

        // Failed dodge: Enemy ATK is halved (10 / 2 = 5).
        // Damage taken: 5 ATK - 4 DEF = 1 Damage.
        CombatTurnResult result = dodgingCombatSystem.dodge(player, enemy);
        assertEquals(99, player.getHealth()); 
        assertTrue(result.getMessage().contains("Alien clips Hero for 1 damage"));
    }
}
