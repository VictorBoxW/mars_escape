package model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Floor {
    private final String name;
    private final List<Room> rooms;
    private final List<Rectangle> walls;
    private final List<PickableItem> items;
    private final List<Door> doors;
    private final int width;
    private final int height;

    public Floor(String name, List<Room> rooms, List<Rectangle> walls, List<PickableItem> items, int width, int height) {
        this.name = name;
        this.rooms = new ArrayList<>(rooms);
        this.walls = new ArrayList<>(walls);
        this.items = new ArrayList<>(items != null ? items : List.of());
        this.doors = new ArrayList<>();
        this.width = width;
        this.height = height;
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public List<Door> getDoors() {
        return Collections.unmodifiableList(doors);
    }

    public void updateDoors() {
        for (Door door : doors) {
            door.update();
        }
    }

    public String getName() {
        return name;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Rectangle> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public List<PickableItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Room getCurrentRoom() {
        return rooms.stream().filter(r -> !r.isCleared()).findFirst().orElse(null);
    }

    public boolean isCleared() {
        return rooms.stream().allMatch(Room::isCleared);
    }

    public Room getRoomAt(int x, int y) {
        for (Room room : rooms) {
            if (room.contains(x, y)) {
                return room;
            }
        }
        return null;
    }

    public boolean isWalkable(int x, int y) {
        // Keep player within floor bounds
        if (x < 0 || x > width || y < 0 || y > height) {
            return false;
        }

        // Check for wall collisions
        // Player is roughly 40x40 in top-down view (based on ScenePanel)
        Rectangle playerBounds = new Rectangle(x - 20, y - 20, 40, 40);
        for (Rectangle wall : walls) {
            if (wall.intersects(playerBounds)) {
                return false;
            }
        }

        // Check for closed doors
        for (Door door : doors) {
            if (!door.isOpen() && door.getBounds().intersects(playerBounds)) {
                return false;
            }
        }
        return true;
    }
}
