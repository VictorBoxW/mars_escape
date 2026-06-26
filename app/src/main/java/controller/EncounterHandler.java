package controller;

import model.Castle;
import model.Enemy;
import model.Floor;
import model.Player;
import model.Room;

/**
 * Handles room encounter detection and boss-gate access checks.
 */
public class EncounterHandler {

    /**
     * Checks if the player's current position triggers a room encounter.
     * Also enforces the boss-gate rule: the boss room cannot be entered
     * until all other rooms on the floor are cleared.
     *
     * @return the encountered enemy, or {@code null} if no encounter occurs
     */
    public Enemy checkEncounters(Player player, Castle castle, GameView gamePanel) {
        Floor floor = castle.getCurrentFloor();
        if (floor == null) return null;

        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room != null && !room.isCleared()) {
            // Boss Gate Check
            if (room.isBossRoom() && !floor.canAccessBoss()) {
                gamePanel.appendLog("System: You have to defeat both Aliens before confronting the Dark Alien boss!");
                return null;
            }

            Enemy enemy = room.nextEnemy().orElse(null);
            if (enemy != null) {
                gamePanel.appendLog("System: Entering " + room.getName());
                gamePanel.appendLog("Encountered: " + enemy.getName());
                gamePanel.appendLog(enemy.getTaunt());
            }
            return enemy;
        }
        return null;
    }
}
