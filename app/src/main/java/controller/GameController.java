package controller;

import java.util.List;
import model.Armor;
import model.Castle;
import model.Consumable;
import model.Enemy;
import model.Floor;
import model.Item;
import model.Player;
import model.Room;
import model.ShieldItem;
import model.Weapon;
import view.GamePanel;

public class GameController {
    private final CombatSystem combatSystem;
    private Player player;
    private Castle castle;
    private Enemy currentEnemy;
    private GamePanel gamePanel;
    private boolean gameOver;

    public GameController() {
        this.combatSystem = new CombatSystem();
    }

    public void setGamePanel(GamePanel gamePanel) {
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
        
        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor != null && currentFloor.isWalkable(nextX, nextY)) {
            player.move(dx, dy);
            checkItemPickups(currentFloor);
            checkEncounters();
            refreshView();
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

    public boolean canContinue() {
        if (gameOver || currentEnemy != null || castle == null) {
            return false;
        }

        Floor floor = castle.getCurrentFloor();
        return floor != null && floor.isCleared();
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

        if (currentEnemy.isBoss()) {
            player.obtainEnergyCrystal();
            castle.takeEnergyCrystal();
            currentEnemy = null;
            gameOver = true;
            gamePanel.appendLog("The astronaut takes the energy crystal from the Core Chamber.");
            gamePanel.appendLog("Mission complete: The fuel is secured. It's time to fly home.");
            
            // Notify UI of victory
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(gamePanel);
            if (window instanceof view.GameWindow) {
                ((view.GameWindow) window).showGameOverDialog(true);
            }
            return;
        }

        Floor floor = castle.getCurrentFloor();
        if (floor == null) {
            currentEnemy = null;
            return;
        }

        Room room = floor.getRoomAt(player.getX(), player.getY());
        if (room == null) {
            // Fallback: search for any room that has this enemy (defensive)
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
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(gamePanel);
        if (window instanceof view.GameWindow) {
            ((view.GameWindow) window).showGameOverDialog(false);
        }
    }

    private void refreshView() {
        gamePanel.refresh();
    }

    private Player createPlayer() {
        Player player = new Player("Astronaut");
        Weapon rifle = new Weapon("Plasma Rifle", "A high-powered military rifle.", 10);
        Armor suit = new Armor("Pressure Suit", "Standard EVA protection.", 2);

        player.addItem(rifle);
        player.addItem(suit);
        player.addItem(new Consumable("Med Kit", "Restores 40 health.", 40));
        player.addItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25));
        player.equipWeapon(rifle);
        player.equipArmor(suit);
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
            walls.add(new java.awt.Rectangle(0, mapSize - 40, mapSize, 40));
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

        // Floor 1
        List<model.PickableItem> items1 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2200, 200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2000, 400)
        );
        List<Room> rooms1 = List.of(
            new Room("Outer Outpost", "Scout hiding.", List.of(new Enemy("Martian Scout", 35, 7, 3, "Krrr!")), List.of(), 100, 1000, 180, 180),
            new Room("Warrior Den", "Warrior watch.", List.of(new Enemy("Martian Warrior", 45, 9, 4, "Halt!")), List.of(), 1000, 100, 180, 180),
            new Room("Exit Gate", "Final guard.", List.of(new Enemy("Martian Elite", 55, 11, 5, "Die!")), List.of(), 2000, 2000, 180, 180)
        );
        floors.add(new Floor("Lower Bastion", rooms1, commonWalls, items1, mapSize, mapSize));

        // Floor 2
        List<model.PickableItem> items2 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2200, 200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2000, 400)
        );
        List<Room> rooms2 = List.of(
            new Room("Research Lab", "Scientist.", List.of(new Enemy("Alien Scientist", 65, 13, 5, "Intrruption!")), List.of(), 100, 1000, 180, 180),
            new Room("Data Vault", "Guardian.", List.of(new Enemy("Alien Guardian", 75, 15, 6, "Aagh!")), List.of(), 1000, 100, 180, 180),
            new Room("Lab Command", "Master.", List.of(new Enemy("Alien Master", 85, 17, 7, "Stronger!")), List.of(), 2000, 2000, 180, 180)
        );
        floors.add(new Floor("Research Wing", rooms2, commonWalls, items2, mapSize, mapSize));

        // Floor 3
        List<model.PickableItem> items3 = List.of(
            new model.PickableItem(new Consumable("Med Kit", "Restores 40 health.", 40), 200, 2000),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2200, 200),
            new model.PickableItem(new ShieldItem("Shield Cell", "Provides +25 energy shield.", 25), 2000, 400)
        );
        List<Room> rooms3 = List.of(
            new Room("Great Hall", "Royal Guard.", List.of(new Enemy("Royal Guard", 100, 20, 8, "Intruder!")), List.of(), 100, 1000, 180, 180),
            new Room("Sanctum", "Sentinel.", List.of(new Enemy("Elite Sentinel", 110, 22, 9, "Stop!")), List.of(), 1000, 100, 180, 180),
            new Room("Core Chamber", "Dark Alien.", List.of(new Enemy("Dark Alien", 300, 35, 18, "THE CRYSTAL IS MINE!", true)), List.of(), 2000, 2000, 300, 300)
        );
        floors.add(new Floor("Throne Floor", rooms3, commonWalls, items3, mapSize, mapSize));

        return new Castle(floors);
    }
}
