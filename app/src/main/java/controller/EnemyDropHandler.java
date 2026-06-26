package controller;

import model.Castle;
import model.Enemy;
import model.Floor;
import model.Item;
import model.Player;
import model.Room;

/**
 * Handles post-defeat logic: finding the next enemy in a room,
 * claiming room rewards, and spawning item drops.
 */
public class EnemyDropHandler {

    /**
     * Processes an enemy defeat. Returns the next enemy to fight,
     * or {@code null} if combat is over.
     */
    public Enemy handleEnemyDefeated(Enemy currentEnemy, Player player,
                                      Castle castle, GameView gamePanel) {
        if (currentEnemy == null) return null;

        Floor floor = castle.getCurrentFloor();
        if (floor == null) return null;

        Room room = findRoomForEnemy(floor, player, currentEnemy);

        Enemy nextEnemy = (room != null) ? room.nextEnemy().orElse(null) : null;
        if (nextEnemy != null) {
            gamePanel.appendLog(nextEnemy.getTaunt());
            return nextEnemy;
        }

        // Room is fully cleared
        if (room != null) {
            claimRoomRewards(room, player, gamePanel);
            trySpawnFloorDrops(floor, player, gamePanel);
        }
        gamePanel.appendLog("Combat over. You are free to move.");
        return null;
    }

    /**
     * Finds the room the defeated enemy belongs to. Falls back to scanning all
     * rooms for the enemy when the player is no longer standing inside it.
     */
    private Room findRoomForEnemy(Floor floor, Player player, Enemy enemy) {
        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room != null) return room;

        return floor.getRooms().stream()
                    .filter(r -> r.getEnemies().contains(enemy))
                    .findFirst()
                    .orElse(null);
    }

    private void claimRoomRewards(Room room, Player player, GameView gamePanel) {
        for (Item item : room.claimRewardsOnce()) {
            player.addItem(item);
            gamePanel.appendLog("Found item: " + item.getName() + " - " + item.getDescription());
        }
    }

    private void trySpawnFloorDrops(Floor floor, Player player, GameView gamePanel) {
        if (floor.isCleared()) {
            floor.spawnClearedDrops(player, gamePanel::appendLog);
        }
    }
}
