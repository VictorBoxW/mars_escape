package controller;

import model.Floor;
import model.PickableItem;
import model.Player;

/**
 * Handles ground-item pickup logic when the player walks over items.
 */
public class ItemPickupHandler {

    public void checkItemPickups(Player player, Floor floor, GameView gamePanel) {
        for (PickableItem pi : floor.getItems()) {
            if (!pi.isPickedUp() && pi.intersects(player.getX(), player.getY(), 40, 40)) {
                pi.setPickedUp(true);
                player.addItem(pi.getItem());
                gamePanel.appendLog("System: You have obtained a " + pi.getItem().getName() + "!");
            }
        }
    }
}
