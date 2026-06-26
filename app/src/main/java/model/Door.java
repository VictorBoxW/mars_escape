package model;

import java.awt.Rectangle;

public class Door implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean open;
    private boolean locked;
    private boolean exitDoor;
    private double animProgress; // 0 = closed, 1 = open
    private final DoorUnlockStrategy unlockStrategy;

    /**
     * Creates a basic, unlocked door.
     *
     * @param x      the X coordinate of the door
     * @param y      the Y coordinate of the door
     * @param width  the width of the door
     * @param height the height of the door
     */
    public Door(int x, int y, int width, int height) {
        this(x, y, width, height, false, false, null);
    }

    /**
     * Creates a door that can be set as initially locked or designated as an exit door.
     *
     * @param x        the X coordinate of the door
     * @param y        the Y coordinate of the door
     * @param width    the width of the door
     * @param height   the height of the door
     * @param locked   true if the door requires unlocking mechanics
     * @param exitDoor true if passing through this door transitions to another level/victory
     */
    public Door(int x, int y, int width, int height, boolean locked, boolean exitDoor) {
        this(x, y, width, height, locked, exitDoor, null);
    }

    /**
     * Creates a fully specified door with a custom unlock strategy.
     *
     * @param x              the X coordinate of the door
     * @param y              the Y coordinate of the door
     * @param width          the width of the door
     * @param height         the height of the door
     * @param locked         true if the door is initially locked
     * @param exitDoor       true if this door acts as a floor or game exit
     * @param unlockStrategy the strategy rule used to unlock this door;
     *                       can be {@code null} if the door is not locked or has
     *                       no specific item requirements.
     */

    public Door(int x, int y, int width, int height, boolean locked, boolean exitDoor, DoorUnlockStrategy unlockStrategy) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
        this.locked = locked;
        this.exitDoor = exitDoor;
        this.unlockStrategy = unlockStrategy;
        this.animProgress = 0;
    }

    public DoorUnlockStrategy getUnlockStrategy() {
        return unlockStrategy;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isExitDoor() {
        return exitDoor;
    }

    public double getOpenProgress() {
        return animProgress;
    }

    public void update() {
        if (open && animProgress < 1.0) {
            animProgress = Math.min(1.0, animProgress + 0.1);
        } else if (!open && animProgress > 0.0) {
            animProgress = Math.max(0.0, animProgress - 0.1);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isNear(int px, int py, int threshold) {
        return distanceTo(px, py) < threshold;
    }

    private double distanceTo(int px, int py) {
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        return Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2));
    }
}
