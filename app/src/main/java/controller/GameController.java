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
        
        player.move(dx, dy);
        checkEncounters();
        refreshView();
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
                gamePanel.appendLog("Ascending to " + castle.getCurrentFloor().getName());
                player.setPosition(440, 500); // Reset position for new floor
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
        Weapon rifle = new Weapon("Plasma Rifle", "A high-powered military rifle.", 8);
        Armor suit = new Armor("Pressure Suit", "Standard EVA protection.", 1);

        player.addItem(rifle);
        player.addItem(suit);
        player.addItem(new Consumable("Med Kit", "Restores 32 health.", 32));
        player.addItem(new ShieldItem("Shield Cell", "Provides +20 energy shield.", 20));
        player.equipWeapon(rifle);
        player.equipArmor(suit);

        return player;
    }

    private Castle createCastle() {
        // Floor 1: 3 Rooms, 1 alien each
        Floor floor1 = new Floor("Lower Bastion", List.of(
                new Room("Outer Guard", "A lone alien stands watch.", List.of(new Enemy("Martian Scout", 20, 5, 1, "Krrr!")), List.of(), 100, 100, 150, 150),
                new Room("Inner Guard", "The hallway is guarded.", List.of(new Enemy("Martian Guard", 25, 6, 2, "Halt!")), List.of(), 350, 100, 150, 150),
                new Room("Stairwell", "A final sentinel before the stairs.", List.of(new Enemy("Martian Elite", 30, 7, 3, "Die!")), List.of(), 600, 100, 150, 150)
        ));

        // Floor 2: 3 Rooms, 1 alien each
        Floor floor2 = new Floor("Royal Wing", List.of(
                new Room("Library", "An alien scholar defends its books.", List.of(new Enemy("Alien Mage", 35, 8, 2, "Magic!")), List.of(), 100, 100, 150, 150),
                new Room("Gallery", "A warrior stands among the art.", List.of(new Enemy("Alien Warrior", 40, 9, 4, "Aagh!")), List.of(), 350, 100, 150, 150),
                new Room("Armory", "The armor master blocks the way.", List.of(new Enemy("Alien Master", 45, 10, 5, "Stronger!")), List.of(), 600, 100, 150, 150)
        ));

        // Floor 3: 3 Rooms, 2 aliens in first 2, then BOSS
        Floor floor3 = new Floor("Throne Floor", List.of(
                new Room("First Gate", "Two guards block the path.", List.of(
                        new Enemy("Elite Guard A", 50, 11, 5, "Intruder!"),
                        new Enemy("Elite Guard B", 50, 11, 5, "Double trouble!")
                ), List.of(), 100, 100, 150, 150),
                new Room("Second Gate", "Another pair of elites.", List.of(
                        new Enemy("Elite Guard C", 55, 12, 6, "Stop!"),
                        new Enemy("Elite Guard D", 55, 12, 6, "No pass!")
                ), List.of(), 350, 100, 150, 150),
                new Room("Throne Room", "The Dark Alien holds the crystal.", List.of(
                        new Enemy("Dark Alien", 150, 18, 10, "THE CRYSTAL IS MINE!", true)
                ), List.of(), 600, 100, 150, 150)
        ));

        return new Castle(List.of(floor1, floor2, floor3));
    }
}
