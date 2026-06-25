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

    public void handleInteraction(Player player, Castle castle, GameView gamePanel) {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor == null) return;

        for (Door door : currentFloor.getDoors()) {
            if (door.isNear(player.getX(), player.getY(), 100)) {
                if (door.isLocked()) {
                    DoorUnlockStrategy strategy = door.getUnlockStrategy();
                    if (strategy != null) {
                        if (strategy.isConditionMet(player)) {
                            strategy.unlock(player, door);
                            gamePanel.appendLog(strategy.getUnlockMessage());
                        } else {
                            gamePanel.appendLog(strategy.getFailureMessage());
                        }
                    }
                } else {
                    door.setOpen(!door.isOpen());
                }
                gamePanel.refresh();
                return;
            }
        }
    }
}
