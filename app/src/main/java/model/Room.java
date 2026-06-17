package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Room implements Interactable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String description;
    private final List<Enemy> enemies;
    private final List<Item> rewards;
    private boolean rewardsClaimed;
    private int x;
    private int y;
    private int width;
    private int height;

    public Room(String name, String description, List<Enemy> enemies, List<Item> rewards, int x, int y, int width, int height) {
        this.name = name;
        this.description = description;
        this.enemies = new ArrayList<>(enemies);
        this.rewards = new ArrayList<>(rewards);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public boolean isCleared() {
        return enemies.stream().noneMatch(Enemy::isAlive);
    }

    public Optional<Enemy> nextEnemy() {
        return enemies.stream().filter(Enemy::isAlive).findFirst();
    }

    public List<Item> claimRewards() {
        if (rewardsClaimed) {
            return List.of();
        }

        rewardsClaimed = true;
        return Collections.unmodifiableList(rewards);
    }

    @Override
    public String interact(Player player) {
        if (isCleared()) {
            return player.getName() + " searches " + name + ". The room is secure.";
        }

        return description;
    }
}
