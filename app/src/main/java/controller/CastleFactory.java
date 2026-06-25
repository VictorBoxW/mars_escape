package controller;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import model.Castle;
import model.Consumable;
import model.Door;
import model.Enemy;
import model.Floor;
import model.PickableItem;
import model.Player;
import model.Room;
import model.ShieldItem;

/**
 * Factory responsible for creating the initial game world (Castle and Player).
 * Extracted from GameController to follow Single Responsibility Principle.
 */
public class CastleFactory {

    private CastleFactory() {
        // utility class
    }

    public static Player createPlayer() {
        Player player = new Player("Astronaut");

        player.addItem(new Consumable("Med Kit", "Restores 40 health.", 40));
        player.addItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25));
        player.setPosition(100, 100);

        return player;
    }

    public static Castle createCastle() {
        List<Floor> floors = new ArrayList<>();
        int mapSize = 2400; // Large labyrinth

        // Helper to add perimeter walls
        Consumer<List<Rectangle>> addPerimeter = (walls) -> {
            walls.add(new Rectangle(0, 0, mapSize, 40));
            walls.add(new Rectangle(0, 0, 40, mapSize));
            // Bottom wall with gap for exit door
            walls.add(new Rectangle(0, mapSize - 40, 2000, 40));
            walls.add(new Rectangle(2180, mapSize - 40, 220, 40));
            walls.add(new Rectangle(mapSize - 40, 0, 40, mapSize));
        };

        // Common wall layout (Maze)
        List<Rectangle> commonWalls = new ArrayList<>();
        addPerimeter.accept(commonWalls);
        commonWalls.add(new Rectangle(400, 0, 40, 600));
        commonWalls.add(new Rectangle(0, 800, 800, 40));
        commonWalls.add(new Rectangle(800, 400, 40, 1200));
        commonWalls.add(new Rectangle(1200, 0, 40, 1600));
        commonWalls.add(new Rectangle(1200, 1600, 800, 40));
        commonWalls.add(new Rectangle(400, 1800, 40, 600));
        commonWalls.add(new Rectangle(1240, 1100, 1120, 40));

        // Floor 1
        List<PickableItem> items1 = List.of(
            new PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
        );
        List<Room> rooms1 = List.of(
            new Room("Outer Outpost", "Scout hiding.", List.of(new Enemy("Martian Scout", 24, 5, "Krrr!")), List.of(), 100, 1000, 180, 180),
            new Room("Warrior Den", "Warrior watch.", List.of(new Enemy("Martian Warrior", 36, 6, "Halt!")), List.of(), 1000, 100, 180, 180),
            new Room("Exit Gate", "Final guard.", List.of(new Enemy("Martian Elite", 48, 7, "Die!")), List.of(), 2000, 2000, 180, 180)
        );
        Floor f1 = new Floor("Lower Bastion", rooms1, commonWalls, items1, mapSize, mapSize);
        // Big brownish doors in maze gaps
        f1.addDoor(new Door(40, 1800, 360, 40));    // Med Kit area entry gap
        f1.addDoor(new Door(2000, 1600, 360, 40));  // Shield area entry gap
        // Key-locked Exit Door in the bottom wall
        f1.addDoor(new Door(2000, 2360, 180, 40, true, true));
        floors.add(f1);

        // Floor 2
        List<PickableItem> items2 = List.of(
            new PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
        );
        List<Room> rooms2 = List.of(
            new Room("Research Lab", "Scientist.", List.of(new Enemy("Alien Scientist", 60, 8, "Intrruption!")), List.of(), 100, 1000, 180, 180),
            new Room("Data Vault", "Guardian.", List.of(new Enemy("Alien Guardian", 72, 9, "Aagh!")), List.of(), 1000, 100, 180, 180),
            new Room("Lab Command", "Master.", List.of(new Enemy("Alien Master", 84, 10, "Stronger!")), List.of(), 2000, 2000, 180, 180)
        );
        Floor f2 = new Floor("Research Wing", rooms2, commonWalls, items2, mapSize, mapSize);
        f2.addDoor(new Door(40, 1800, 360, 40));
        f2.addDoor(new Door(2000, 1600, 360, 40));
        // Key-locked Exit Door in the bottom wall
        f2.addDoor(new Door(2000, 2360, 180, 40, true, true));
        floors.add(f2);

        // Floor 3
        List<PickableItem> items3 = List.of(
            new PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
        );
        List<Room> rooms3 = List.of(
            new Room("Great Hall", "Royal Guard.", List.of(new Enemy("Royal Guard", 84, 12, "Intruder!")), List.of(), 100, 1000, 180, 180),
            new Room("Sanctum", "Sentinel.", List.of(new Enemy("Elite Sentinel", 96, 13, "Stop!")), List.of(), 1000, 100, 180, 180),
            new Room("Core Chamber", "Dark Alien.", List.of(new Enemy("Dark Alien", 120, 14, "THE CRYSTAL IS MINE!", true)), List.of(), 2000, 2000, 300, 300)
        );
        Floor f3 = new Floor("Throne Floor", rooms3, commonWalls, items3, mapSize, mapSize);
        f3.addDoor(new Door(40, 1800, 360, 40));
        f3.addDoor(new Door(2000, 1600, 360, 40));
        // Key-locked Exit Door on Floor 3
        f3.addDoor(new Door(2000, 2360, 180, 40, true, true));
        floors.add(f3);

        return new Castle(floors);
    }
}
