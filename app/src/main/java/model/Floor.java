package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Floor {
    private final String name;
    private final List<Room> rooms;
    private int currentRoomIndex;

    public Floor(String name, List<Room> rooms) {
        this.name = name;
        this.rooms = new ArrayList<>(rooms);
    }

    public String getName() {
        return name;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
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
}
