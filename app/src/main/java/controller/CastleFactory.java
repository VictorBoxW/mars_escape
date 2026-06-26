package controller;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import model.*;

/**
 * Factory responsible for creating the initial game world (Castle and Player).
 * Extracted from GameController to follow Single Responsibility Principle.
 */
public class CastleFactory {

    private static final int MAP_SIZE = 2400; // Large labyrinth (width and height)

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

        floors.add(createFloor1());
        floors.add(createFloor2());
        floors.add(createFloor3());

        return new Castle(floors);
    }

    private static Floor createFloor1() {
        List<Room> rooms = List.of(
            new Room("Outer Outpost", List.of(new Enemy("Martian Scout", 24, 5, "Krrr!")), List.of(), 100, 1000, 180, 180),
            new Room("Warrior Den", List.of(new Enemy("Martian Warrior", 36, 6, "Halt!")), List.of(), 1000, 100, 180, 180),
            new Room("Exit Gate", List.of(new Enemy("Martian Elite", 48, 7, "Die!")), List.of(), 2000, 2000, 180, 180)
        );
        Floor floor = new Floor("Lower Bastion", rooms, createMazeWalls(), createStandardFloorItems(), MAP_SIZE, MAP_SIZE, new KeyDropBehavior(1));
        addMazeDoors(floor);
        floor.addDoor(createExitDoor(new StandardDoorUnlockStrategy(1)));
        return floor;
    }

    private static Floor createFloor2() {
        List<Room> rooms = List.of(
            new Room("Research Lab", List.of(new Enemy("Alien Scientist", 60, 8, "Intrruption!")), List.of(), 100, 1000, 180, 180),
            new Room("Data Vault", List.of(new Enemy("Alien Guardian", 72, 9, "Aagh!")), List.of(), 1000, 100, 180, 180),
            new Room("Lab Command", List.of(new Enemy("Alien Master", 84, 10, "Stronger!")), List.of(), 2000, 2000, 180, 180)
        );
        Floor floor = new Floor("Research Wing", rooms, createMazeWalls(), createStandardFloorItems(), MAP_SIZE, MAP_SIZE, new KeyDropBehavior(2));
        addMazeDoors(floor);
        floor.addDoor(createExitDoor(new StandardDoorUnlockStrategy(2)));
        return floor;
    }

    private static Floor createFloor3() {
        List<Room> rooms = List.of(
            new Room("Great Hall", List.of(new Enemy("Royal Guard", 84, 12, "Intruder!")), List.of(), 100, 1000, 180, 180),
            new Room("Sanctum", List.of(new Enemy("Elite Sentinel", 96, 13, "Stop!")), List.of(), 1000, 100, 180, 180),
            new Room("Core Chamber", List.of(new Enemy("Dark Alien", 120, 14, "THE CRYSTAL IS MINE!", true)), List.of(), 2000, 2000, 300, 300, true)
        );
        Floor floor = new Floor("Throne Floor", rooms, createMazeWalls(), createStandardFloorItems(), MAP_SIZE, MAP_SIZE, new BossDropBehavior(3));
        addMazeDoors(floor);
        floor.addDoor(createExitDoor(new FinalFloorDoorUnlockStrategy(3)));
        return floor;
    }

    /** The three standard pickups (Med Kit + two Shield Cells) shared by every floor. */
    private static List<PickableItem> createStandardFloorItems() {
        return List.of(
            new PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
        );
    }

    /** Builds the maze wall layout (perimeter + inner corridors) shared by every floor. */
    private static List<Rectangle> createMazeWalls() {
        List<Rectangle> walls = new ArrayList<>();
        addPerimeterWalls(walls);
        walls.add(new Rectangle(400, 0, 40, 600));
        walls.add(new Rectangle(0, 800, 800, 40));
        walls.add(new Rectangle(800, 400, 40, 1200));
        walls.add(new Rectangle(1200, 0, 40, 1600));
        walls.add(new Rectangle(1200, 1600, 800, 40));
        walls.add(new Rectangle(400, 1800, 40, 600));
        walls.add(new Rectangle(1240, 1100, 1120, 40));
        return walls;
    }

    private static void addPerimeterWalls(List<Rectangle> walls) {
        walls.add(new Rectangle(0, 0, MAP_SIZE, 40));
        walls.add(new Rectangle(0, 0, 40, MAP_SIZE));
        // Bottom wall, split to leave a gap for the exit door
        walls.add(new Rectangle(0, MAP_SIZE - 40, 2000, 40));
        walls.add(new Rectangle(2180, MAP_SIZE - 40, 220, 40));
        walls.add(new Rectangle(MAP_SIZE - 40, 0, 40, MAP_SIZE));
    }

    /** Adds the two brown doors that gate the Med Kit and Shield maze pockets. */
    private static void addMazeDoors(Floor floor) {
        floor.addDoor(new Door(40, 1800, 360, 40));    // Med Kit area entry gap
        floor.addDoor(new Door(2000, 1600, 360, 40));  // Shield area entry gap
    }

    /** Creates the key-locked exit door positioned in the bottom-wall gap. */
    private static Door createExitDoor(DoorUnlockStrategy unlockStrategy) {
        return new Door(2000, 2360, 180, 40, true, true, unlockStrategy);
    }
}
