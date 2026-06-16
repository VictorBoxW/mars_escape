package model;

import java.awt.Rectangle;

public class Door {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean open;
    private boolean locked;
    private boolean exitDoor;
    private double animProgress; // 0 = closed, 1 = open

    public Door(int x, int y, int width, int height) {
        this(x, y, width, height, false, false);
    }

    public Door(int x, int y, int width, int height, boolean locked, boolean exitDoor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
        this.locked = locked;
        this.exitDoor = exitDoor;
        this.animProgress = 0;
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

    public double getAnimProgress() {
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
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double distance = Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2));
        return distance < threshold;
    }
}
