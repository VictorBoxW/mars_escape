package controller;

import model.Floor;
import model.PickableItem;
import model.Player;

/**
 * Handles ground-item pickup logic when the player walks over items.
 */
public class ItemPickupHandler {

    private static final int PLAYER_SIZE = 40;

    public void checkItemPickups(Player player, Floor floor, GameView gamePanel) {
        for (PickableItem pickable : floor.getItems()) {
            if (!pickable.isPickedUp()
                    && pickable.isOverlappedByPlayer(player.getX(), player.getY(), PLAYER_SIZE)) {
                pickUp(pickable, player, gamePanel);
            }
        }
    }

    private void pickUp(PickableItem pickable, Player player, GameView gamePanel) {
        pickable.setPickedUp(true);
        player.addItem(pickable.getItem());
        gamePanel.appendLog("System: You have obtained a " + pickable.getItem().getName() + "!");
    }
}
