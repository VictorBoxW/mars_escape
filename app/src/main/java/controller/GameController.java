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
                gamePanel.appendLog("System: Ascending to " + castle.getCurrentFloor().getName());
                player.setPosition(50, 450); // Reset position for new floor
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
        player.setPosition(50, 450);

        return player;
    }

    private Castle createCastle() {
        // Floor 1: 3 Rooms, 1 alien each
        Floor floor1 = new Floor("Lower Bastion", List.of(
                new Room("North Guard", "A lone scout.", List.of(new Enemy("Martian Scout", 30, 6, 2, "Krrr!")), List.of(), 100, 50, 160, 160),
                new Room("Central Hall", "A warrior watch.", List.of(new Enemy("Martian Warrior", 40, 8, 3, "Halt!")), List.of(), 360, 220, 160, 160),
                new Room("South Exit", "Final guard.", List.of(new Enemy("Martian Elite", 50, 10, 4, "Die!")), List.of(), 620, 390, 160, 160)
        ));

        // Floor 2: 3 Rooms, 1 alien each
        Floor floor2 = new Floor("Research Wing", List.of(
                new Room("Lab Alpha", "Researcher.", List.of(new Enemy("Alien Scientist", 55, 12, 4, "Interruption!")), List.of(), 100, 390, 160, 160),
                new Room("Data Core", "The curator.", List.of(new Enemy("Alien Guardian", 65, 14, 5, "Aagh!")), List.of(), 360, 220, 160, 160),
                new Room("Lab Beta", "Experiment protector.", List.of(new Enemy("Alien Master", 75, 16, 6, "Stronger!")), List.of(), 620, 50, 160, 160)
        ));

        // Floor 3: 3 Rooms, 1 alien each
        Floor floor3 = new Floor("Throne Floor", List.of(
                new Room("Great Hall", "Heavy guard.", List.of(new Enemy("Royal Guard", 85, 18, 7, "Intruder!")), List.of(), 100, 50, 160, 160),
                new Room("Ante-Chamber", "Sentinel.", List.of(new Enemy("Elite Sentinel", 95, 20, 8, "Stop!")), List.of(), 360, 220, 160, 160),
                new Room("Throne Room", "The Dark Alien holds the crystal.", List.of(new Enemy("Dark Alien", 200, 25, 12, "THE CRYSTAL IS MINE!", true)), List.of(), 620, 390, 160, 160)
        ));

        return new Castle(List.of(floor1, floor2, floor3));
    }
}
