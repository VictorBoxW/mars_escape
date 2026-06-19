package controller;

import java.util.List;
import model.Castle;
import model.Consumable;
import model.Diamond;
import model.Door;
import model.Enemy;
import model.Floor;
import model.Item;
import model.Key;
import model.Player;
import model.Room;
import model.ShieldItem;
import model.GameState;

public class GameController {
    private final CombatSystem combatSystem;
    private Player player;
    private Castle castle;
    private Enemy currentEnemy;
    private GameView gamePanel;
    private boolean gameOver;

    public GameController() {
        this.combatSystem = new CombatSystem();
    }

    public void setGamePanel(GameView gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void restartGame() {
        player = createPlayer();
        castle = createCastle();
        currentEnemy = null;
        gameOver = false;

        gamePanel.setLog("Mars Escape\n"
                + "An astronaut is stranded on Mars. The nearby alien fortress holds an energy crystal needed for fuel.\n");
        checkEncounters();
        refreshView();
    }

    public void attack() {
        if (!canFight()) {
            return;
        }

        resolveCombatTurn(combatSystem.attack(player, currentEnemy));
    }

    public void dodge() {
        if (!canFight()) {
            return;
        }

        resolveCombatTurn(combatSystem.dodge(player, currentEnemy));
    }

    public void useItem(int itemIndex) {
        if (gameOver || player == null || !player.isAlive()) {
            return;
        }

        CombatTurnResult result = combatSystem.useItem(player, currentEnemy, itemIndex);
        gamePanel.appendLog(result.getMessage());

        if (result.isPlayerDefeated()) {
            failMission();
        }

        refreshView();
    }

    public void movePlayer(int dx, int dy) {
        if (gameOver || currentEnemy != null) return;
        
        int nextX = player.getX() + dx;
        int nextY = player.getY() + dy;
        
        // Update rotation based on direction
        if (dx != 0 || dy != 0) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
            player.setRotation(angle);
            player.updateWalkPhase(0.3); // Progress animation
        }

        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor != null) {
            // Check for exit door transition
            for (Door door : currentFloor.getDoors()) {
                if (door.isExitDoor() && door.isOpen() && door.getBounds().contains(nextX, nextY)) {
                    if (castle.getCurrentFloorNumber() == castle.getFloorCount()) {
                        // VICTORY on Floor 3
                        gamePanel.appendLog("The astronaut takes the Energy Diamond and the fuel key.");
                        gamePanel.appendLog("Mission Success: The supplies are secured. It's time to fly home!");
                        gameOver = true;
                        if (gamePanel != null) {
                            gamePanel.showGameOverDialog(true);
                        }
                    } else {
                        continueExploring(); // Advance to next floor
                    }
                    return;
                }
            }

            if (currentFloor.isWalkable(nextX, nextY)) {
                player.move(dx, dy);
                checkItemPickups(currentFloor);
                checkEncounters();
                refreshView();
            }
        }
    }

    public void update() {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor != null) {
            currentFloor.updateDoors();
            refreshView();
        }
    }

    /**
     * Returns true if any door is currently mid-animation
     * (i.e. its visual state hasn't caught up with its logical state).
     * Used by the timer to avoid full repaints when everything is idle.
     */
    public boolean hasPendingAnimations() {
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor == null) return false;
        for (Door door : currentFloor.getDoors()) {
            double progress = door.getAnimProgress();
            if (door.isOpen() && progress < 1.0) return true;
            if (!door.isOpen() && progress > 0.0) return true;
        }
        return false;
    }
    public void interact() {
        if (gameOver || currentEnemy != null) return;

        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor != null) {
            for (Door door : currentFloor.getDoors()) {
                if (door.isNear(player.getX(), player.getY(), 100)) {
                    if (door.isLocked()) {
                        // Check for requirements
                        if (castle.getCurrentFloorNumber() == 3) {
                            // Floor 3 needs Key AND Diamond
                            Key key = (Key) player.getInventory().stream()
                                .filter(i -> i instanceof Key && ((Key)i).getFloorLevel() == 3)
                                .findFirst().orElse(null);
                            Diamond diamond = (Diamond) player.getInventory().stream()
                                .filter(i -> i instanceof Diamond)
                                .findFirst().orElse(null);

                            if (key != null && diamond != null) {
                                door.setLocked(false);
                                door.setOpen(true);
                                player.removeItem(key);
                                player.removeItem(diamond);
                                gamePanel.appendLog("System: Exit Door unlocked using the Diamond and Key! Items consumed.");
                            } else {
                                gamePanel.appendLog("System: You have to obtain both the Key and the Diamond to open this door.");
                            }
                        } else {
                            // Other floors just need Key
                            Key key = (Key) player.getInventory().stream()
                                .filter(i -> i instanceof Key && ((Key)i).getFloorLevel() == castle.getCurrentFloorNumber())
                                .findFirst().orElse(null);
                            
                            if (key != null) {
                                door.setLocked(false);
                                door.setOpen(true);
                                player.removeItem(key);
                                gamePanel.appendLog("System: Door unlocked using the Access Key! Key consumed.");
                            } else {
                                gamePanel.appendLog("System: You have to obtain the key in order to open the door.");
                            }
                        }
                    } else {
                        door.setOpen(!door.isOpen());
                    }
                    refreshView();
                    return;
                }
            }
        }
    }

    private void checkItemPickups(Floor floor) {
        for (model.PickableItem pi : floor.getItems()) {
            if (!pi.isPickedUp() && pi.intersects(player.getX(), player.getY(), 40, 40)) {
                pi.setPickedUp(true);
                player.addItem(pi.getItem());
                gamePanel.appendLog("System: You have obtained a " + pi.getItem().getName() + "!");
            }
        }
    }

    public void continueExploring() {
        if (gameOver || currentEnemy != null) {
            return;
        }

        Floor floor = castle.getCurrentFloor();
        if (floor == null) {
            return;
        }

        if (floor.isCleared()) {
            if (castle.advanceFloor()) {
                gamePanel.appendLog("System: Ascending to " + castle.getCurrentFloor().getName());
                player.setPosition(100, 100); // Start of floor
            } else if (!castle.isEnergyCrystalTaken()) {
                gamePanel.appendLog("The fortress is quiet, but the crystal is still missing.");
            }
        }

        refreshView();
    }

    public Player getPlayer() {
        return player;
    }

    public Castle getCastle() {
        return castle;
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean canFight() {
        return !gameOver && player != null && player.isAlive() && currentEnemy != null && currentEnemy.isAlive();
    }

    private void resolveCombatTurn(CombatTurnResult result) {
        String logMessage = result.getMessage().replace("Jack", "The astronaut");
        gamePanel.appendLog(logMessage);

        if (result.isPlayerDefeated()) {
            failMission();
        } else if (result.isEnemyDefeated()) {
            handleEnemyDefeated();
        }

        refreshView();
    }

    private void handleEnemyDefeated() {
        if (currentEnemy == null) return;

        Floor floor = castle.getCurrentFloor();
        if (floor == null) {
            currentEnemy = null;
            return;
        }

        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room == null) {
            room = floor.getRooms().stream()
                        .filter(r -> r.getEnemies().contains(currentEnemy))
                        .findFirst()
                        .orElse(null);
        }

        if (room != null) {
            currentEnemy = room.nextEnemy().orElse(null);
        } else {
            currentEnemy = null;
        }

        if (currentEnemy != null) {
            gamePanel.appendLog(currentEnemy.getTaunt());
            return;
        }

        // Room is fully cleared
        currentEnemy = null;
        if (room != null) {
            claimRoomRewards(room);
            
            // Handle drops based on floor
            if (floor.isCleared()) {
                if (castle.getCurrentFloorNumber() == 3) {
                    // Final Boss drops Key and Diamond
                    Key key = new Key(3);
                    Diamond diamond = new Diamond();
                    floor.addItem(new model.PickableItem(key, player.getX() + 40, player.getY()));
                    floor.addItem(new model.PickableItem(diamond, player.getX() - 40, player.getY()));
                    gamePanel.appendLog("System: The Dark Alien has dropped the fuel key and the ENERGY DIAMOND!");
                } else {
                    // Floor 1 & 2 drop Key
                    Key key = new Key(castle.getCurrentFloorNumber());
                    floor.addItem(new model.PickableItem(key, player.getX() + 40, player.getY()));
                    gamePanel.appendLog("System: The final guardian has dropped a glowing Access Key!");
                }
            }
        }
        gamePanel.appendLog("Combat over. You are free to move.");
    }

    private void claimRoomRewards(Room room) {
        for (Item item : room.claimRewards()) {
            player.addItem(item);
            gamePanel.appendLog("Found item: " + item.getName() + " - " + item.getDescription());
        }
    }

    private void checkEncounters() {
        Floor floor = castle.getCurrentFloor();
        if (floor == null) return;

        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room != null && !room.isCleared()) {
            // Boss Gate Check
            if (room.getName().equals("Core Chamber") && !floor.canAccessBoss()) {
                gamePanel.appendLog("System: You have to defeat both Aliens before confronting the Dark Alien boss!");
                return;
            }

            currentEnemy = room.nextEnemy().orElse(null);
            if (currentEnemy != null) {
                gamePanel.appendLog("System: Entering " + room.getName());
                gamePanel.appendLog("Encountered: " + currentEnemy.getName());
                gamePanel.appendLog(currentEnemy.getTaunt());
            }
        }
    }

    private void failMission() {
        gameOver = true;
        currentEnemy = null;
        gamePanel.appendLog("Mission failed. The astronaut has fallen in the fortress.");
        
        // Notify UI of defeat
        if (gamePanel != null) {
            gamePanel.showGameOverDialog(false);
        }
    }

    private void refreshView() {
        gamePanel.refresh();
    }

    private Player createPlayer() {
        Player player = new Player("Astronaut");

        player.addItem(new Consumable("Med Kit", "Restores 40 health.", 40));
        player.addItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25));
        player.setPosition(100, 100);

        return player;
    }

    private Castle createCastle() {
        List<Floor> floors = new java.util.ArrayList<>();
        int mapSize = 2400; // Large labyrinth

        // Helper to add perimeter walls
        java.util.function.Consumer<List<java.awt.Rectangle>> addPerimeter = (walls) -> {
            walls.add(new java.awt.Rectangle(0, 0, mapSize, 40));
            walls.add(new java.awt.Rectangle(0, 0, 40, mapSize));
            // Bottom wall with gap for exit door
            walls.add(new java.awt.Rectangle(0, mapSize - 40, 2000, 40));
            walls.add(new java.awt.Rectangle(2180, mapSize - 40, 220, 40));
            walls.add(new java.awt.Rectangle(mapSize - 40, 0, 40, mapSize));
        };

        // Common wall layout (Maze)
        List<java.awt.Rectangle> commonWalls = new java.util.ArrayList<>();
        addPerimeter.accept(commonWalls);
        commonWalls.add(new java.awt.Rectangle(400, 0, 40, 600));
        commonWalls.add(new java.awt.Rectangle(0, 800, 800, 40));
        commonWalls.add(new java.awt.Rectangle(800, 400, 40, 1200));
        commonWalls.add(new java.awt.Rectangle(1200, 0, 40, 1600));
        commonWalls.add(new java.awt.Rectangle(1200, 1600, 800, 40));
        commonWalls.add(new java.awt.Rectangle(400, 1800, 40, 600));
        commonWalls.add(new java.awt.Rectangle(1240, 1100, 1120, 40));

        // Floor 1
        List<model.PickableItem> items1 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
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
        List<model.PickableItem> items2 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
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
        List<model.PickableItem> items3 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2280, 1200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 1300, 1540)
        );
        List<Room> rooms3 = List.of(
            new Room("Great Hall", "Royal Guard.", List.of(new Enemy("Royal Guard", 84, 12, "Intruder!")), List.of(), 100, 1000, 180, 180),
            new Room("Sanctum", "Sentinel.", List.of(new Enemy("Elite Sentinel", 96, 13, "Stop!")), List.of(), 1000, 100, 180, 180),
            new Room("Core Chamber", "Dark Alien.", List.of(new Enemy("Dark Alien", 120, 14,  "THE CRYSTAL IS MINE!", true)), List.of(), 2000, 2000, 300, 300)
        );
        Floor f3 = new Floor("Throne Floor", rooms3, commonWalls, items3, mapSize, mapSize);
        f3.addDoor(new Door(40, 1800, 360, 40));
        f3.addDoor(new Door(2000, 1600, 360, 40));
        // Key-locked Exit Door on Floor 3
        f3.addDoor(new Door(2000, 2360, 180, 40, true, true));
        floors.add(f3);

        return new Castle(floors);
    }

    public void saveGame(String filepath) throws java.io.IOException {
        persistence.PersistenceManager pm = new persistence.FilePersistenceManager();
        String logText = (gamePanel != null) ? gamePanel.getLogText() : "";
        GameState state = new GameState(player, castle, currentEnemy, gameOver, logText);
        pm.save(state, filepath);
    }

    public void loadGame(String filepath) throws java.io.IOException, ClassNotFoundException {
        persistence.PersistenceManager pm = new persistence.FilePersistenceManager();
        GameState state = pm.load(filepath);
        this.player = state.getPlayer();
        this.castle = state.getCastle();
        this.currentEnemy = state.getCurrentEnemy();
        this.gameOver = state.isGameOver();
        
        if (gamePanel != null) {
            gamePanel.setLog(state.getLogHistory());
            gamePanel.refresh();
        }
    }
}
