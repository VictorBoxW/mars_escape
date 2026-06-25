package controller;

import model.Castle;
import model.Diamond;
import model.Enemy;
import model.Floor;
import model.Item;
import model.Key;
import model.PickableItem;
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

        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room == null) {
            room = floor.getRooms().stream()
                        .filter(r -> r.getEnemies().contains(currentEnemy))
                        .findFirst()
                        .orElse(null);
        }

        Enemy nextEnemy;
        if (room != null) {
            nextEnemy = room.nextEnemy().orElse(null);
        } else {
            nextEnemy = null;
        }

        if (nextEnemy != null) {
            gamePanel.appendLog(nextEnemy.getTaunt());
            return nextEnemy;
        }

        // Room is fully cleared
        if (room != null) {
            claimRoomRewards(room, player, gamePanel);
            spawnDrops(floor, castle, player, gamePanel);
        }
        gamePanel.appendLog("Combat over. You are free to move.");
        return null;
    }

    private void claimRoomRewards(Room room, Player player, GameView gamePanel) {
        for (Item item : room.claimRewards()) {
            player.addItem(item);
            gamePanel.appendLog("Found item: " + item.getName() + " - " + item.getDescription());
        }
    }

    private void spawnDrops(Floor floor, Castle castle, Player player, GameView gamePanel) {
        if (floor.isCleared()) {
            if (castle.getCurrentFloorNumber() == 3) {
                // Final Boss drops Key and Diamond
                Key key = new Key(3);
                Diamond diamond = new Diamond();
                floor.addItem(new PickableItem(key, player.getX() + 40, player.getY()));
                floor.addItem(new PickableItem(diamond, player.getX() - 40, player.getY()));
                gamePanel.appendLog("System: The Dark Alien has dropped the fuel key and the ENERGY DIAMOND!");
            } else {
                // Floor 1 & 2 drop Key
                Key key = new Key(castle.getCurrentFloorNumber());
                floor.addItem(new PickableItem(key, player.getX() + 40, player.getY()));
                gamePanel.appendLog("System: The final guardian has dropped a glowing Access Key!");
            }
        }
    }
}
