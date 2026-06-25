package controller;

import model.Castle;
import model.Door;
import model.Floor;
import model.Item;
import model.ItemType;
import model.Key;
import model.Player;

/**
 * Handles player interactions with doors, including locked-door unlock logic.
 * Uses ItemType enum instead of instanceof checks.
 */
public class InteractionHandler {

    public void handleInteraction(Player player, Castle castle, GameView gamePanel) {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor == null) return;

        for (Door door : currentFloor.getDoors()) {
            if (door.isNear(player.getX(), player.getY(), 100)) {
                if (door.isLocked()) {
                    if (castle.getCurrentFloorNumber() == 3) {
                        handleFinalFloorDoor(player, door, gamePanel);
                    } else {
                        handleStandardDoor(player, castle.getCurrentFloorNumber(), door, gamePanel);
                    }
                } else {
                    door.setOpen(!door.isOpen());
                }
                gamePanel.refresh();
                return;
            }
        }
    }

    private void handleFinalFloorDoor(Player player, Door door, GameView gamePanel) {
        // Floor 3 needs Key AND Diamond
        Key key = findKey(player, 3);
        Item diamond = findDiamond(player);

        if (key != null && diamond != null) {
            door.setLocked(false);
            door.setOpen(true);
            player.removeItem(key);
            player.removeItem(diamond);
            gamePanel.appendLog("System: Exit Door unlocked using the Diamond and Key! Items consumed.");
        } else {
            gamePanel.appendLog("System: You have to obtain both the Key and the Diamond to open this door.");
        }
    }

    private void handleStandardDoor(Player player, int floorNumber, Door door, GameView gamePanel) {
        // Other floors just need Key
        Key key = findKey(player, floorNumber);

        if (key != null) {
            door.setLocked(false);
            door.setOpen(true);
            player.removeItem(key);
            gamePanel.appendLog("System: Door unlocked using the Access Key! Key consumed.");
        } else {
            gamePanel.appendLog("System: You have to obtain the key in order to open the door.");
        }
    }

    private Key findKey(Player player, int floorLevel) {
        return player.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.KEY)
                .map(i -> (Key) i)
                .filter(k -> k.getFloorLevel() == floorLevel)
                .findFirst().orElse(null);
    }

    private Item findDiamond(Player player) {
        return player.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.DIAMOND)
                .findFirst().orElse(null);
    }
}
