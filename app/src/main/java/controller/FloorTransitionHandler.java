package controller;

import model.Castle;
import model.Door;
import model.Floor;
import model.Player;

/**
 * Handles floor transitions: exit-door detection, floor advancement,
 * and victory conditions.
 */
public class FloorTransitionHandler {

    public enum TransitionResult {
        /** No exit door triggered. */
        NONE,
        /** Non-final exit door triggered — caller should continue exploring. */
        CONTINUE,
        /** Final exit door triggered — game is won. */
        VICTORY
    }

    /**
     * Checks whether the target position triggers an exit-door transition.
     *
     * @return the transition result indicating what happened
     */
    public TransitionResult checkExitDoorTransition(Player player, Castle castle,
                                                     int nextX, int nextY,
                                                     GameView gamePanel) {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor == null) return TransitionResult.NONE;

        for (Door door : currentFloor.getDoors()) {
            if (door.isExitDoor() && door.isOpen() && door.getBounds().contains(nextX, nextY)) {
                return resolveExitDoorTransition(castle, gamePanel);
            }
        }
        return TransitionResult.NONE;
    }

    private TransitionResult resolveExitDoorTransition(Castle castle, GameView gamePanel) {
        boolean onFinalFloor = castle.getCurrentFloorNumber() == castle.getFloorCount();
        if (!onFinalFloor) {
            return TransitionResult.CONTINUE;
        }

        gamePanel.appendLog("The astronaut takes the Energy Diamond and the fuel key.");
        gamePanel.appendLog("Mission Success: The supplies are secured. It's time to fly home!");
        gamePanel.showGameOverDialog(true);
        return TransitionResult.VICTORY;
    }

    /**
     * Advances to the next floor if the current floor is cleared.
     */
    public void continueExploring(Player player, Castle castle, GameView gamePanel) {
        Floor floor = castle.getCurrentFloor();
        if (floor == null) return;

        if (floor.isCleared()) {
            if (castle.advanceFloor()) {
                gamePanel.appendLog("System: Ascending to " + castle.getCurrentFloor().getName());
                player.setPosition(100, 100);
            } else {
                gamePanel.appendLog("The fortress is quiet, but the crystal is still missing.");
            }
        }
    }
}
