package model;

import java.awt.Rectangle;

public class PickableItem implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Item item;
    private final int x;
    private final int y;
    private final int width = 30;
    private final int height = 30;
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
        return new Rectangle(x, y, width, height);
    }

    public boolean intersects(int px, int py, int pw, int ph) {
        return getBounds().intersects(new Rectangle(px - pw / 2, py - ph / 2, pw, ph));
    }
}
