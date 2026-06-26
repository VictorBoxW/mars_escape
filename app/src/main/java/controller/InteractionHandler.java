package controller;

import model.Castle;
import model.Door;
import model.Floor;
import model.Player;
import model.DoorUnlockStrategy;

/**
 * Handles player interactions with doors, including locked-door unlock logic.
 */
public class InteractionHandler {

    private static final int DOOR_INTERACTION_RANGE = 100;

    public void handleInteraction(Player player, Castle castle, GameView gamePanel) {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor == null) return;

        for (Door door : currentFloor.getDoors()) {
            if (door.isNear(player.getX(), player.getY(), DOOR_INTERACTION_RANGE)) {
                if (door.isLocked()) {
                    handleLockedDoor(door, player, gamePanel);
                } else {
                    door.setOpen(!door.isOpen());
                }
                gamePanel.refresh();
                return;
            }
        }
    }

    private void handleLockedDoor(Door door, Player player, GameView gamePanel) {
        DoorUnlockStrategy strategy = door.getUnlockStrategy();
        if (strategy == null) return;

        if (strategy.isConditionMet(player)) {
            strategy.unlock(player, door);
            gamePanel.appendLog(strategy.getUnlockMessage());
        } else {
            gamePanel.appendLog(strategy.getFailureMessage());
        }
    }
}
