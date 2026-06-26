package model;

import java.awt.Rectangle;

public class PickableItem implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Item item;
    private final int x;
    private final int y;
    private static final int ITEM_SIZE = 30;
    private boolean pickedUp;

    public PickableItem(Item item, int x, int y) {
        this.item = item;
        this.x = x;
        this.y = y;
        this.pickedUp = false;
    }

    public Item getItem() {
        return item;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, ITEM_SIZE, ITEM_SIZE);
    }

    public boolean isOverlappedByPlayer(int px, int py, int playerSize) {
        return getBounds().intersects(new Rectangle(px - playerSize / 2, py - playerSize / 2, playerSize, playerSize));
    }
}
