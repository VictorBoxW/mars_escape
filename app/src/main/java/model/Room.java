package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Room implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final List<Enemy> enemies;
    private final List<Item> rewards;
    private boolean rewardsClaimed;
    private int x;
    private int y;
    private int width;
    private int height;
    private final boolean bossRoom;

    public Room(String name, List<Enemy> enemies, List<Item> rewards, int x, int y, int width, int height) {
        this(name, enemies, rewards, x, y, width, height, false);
    }

    public Room(String name, List<Enemy> enemies, List<Item> rewards, int x, int y, int width, int height, boolean bossRoom) {
        this.name = name;
        this.enemies = new ArrayList<>(enemies);
        this.rewards = new ArrayList<>(rewards);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bossRoom = bossRoom;
    }

    public boolean isBossRoom() {
        return bossRoom;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public String getName() {
        return name;
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

    /**
     * Returns the room's reward items and marks them as claimed.
     * Subsequent calls return an empty list.
     */
    public List<Item> claimRewardsOnce() {
        if (rewardsClaimed) {
            return List.of();
        }

        rewardsClaimed = true;
        return Collections.unmodifiableList(rewards);
    }

}
