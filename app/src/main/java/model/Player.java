package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player extends Character {
    private static final long serialVersionUID = 1L;
    private final List<Item> inventory;
    private boolean hasEnergyCrystal;
    private int x;
    private int y;
    private double rotation;
    private double walkPhase;

    public Player(String name) {
        super(name, 100, 12, 2);
        this.inventory = new ArrayList<>();
        this.x = 440; // Center
        this.y = 550; // Near bottom
        this.rotation = 0; // Facing Up
        this.walkPhase = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getWalkPhase() {
        return walkPhase;
    }

    public void updateWalkPhase(double delta) {
        this.walkPhase += delta;
        if (this.walkPhase > Math.PI * 2) {
            this.walkPhase -= Math.PI * 2;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void addItem(Item item) {
        if (item != null) {
            inventory.add(item);
        }
    }

    public void removeItem(Item item) {
        inventory.remove(item);
    }

    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    public String useItem(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= inventory.size()) {
            return "No usable item selected.";
        }

        Item item = inventory.get(itemIndex);
        return item.interact(this);
    }

    public void obtainEnergyCrystal() {
        hasEnergyCrystal = true;
    }

    public boolean hasEnergyCrystal() {
        return hasEnergyCrystal;
    }
}
