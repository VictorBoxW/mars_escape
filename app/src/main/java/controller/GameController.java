package controller;

import model.Castle;
import model.Enemy;
import model.Floor;
import model.Player;
import model.GameState;
import persistence.PersistenceManager;

import java.io.IOException;

public class GameController {
    private final CombatSystem combatSystem;
    private final PersistenceManager persistenceManager;
    private final InteractionHandler interactionHandler;
    private final FloorTransitionHandler floorTransitionHandler;
    private final EnemyDropHandler enemyDropHandler;
    private final ItemPickupHandler itemPickupHandler;
    private final EncounterHandler encounterHandler;

    private Player player;
    private Castle castle;
    private Enemy currentEnemy;
    private GameView gamePanel;
    private boolean gameOver;

    public GameController() {
        this(new CombatSystem(), new persistence.FilePersistenceManager(),
                new InteractionHandler(), new FloorTransitionHandler(),
                new EnemyDropHandler(), new ItemPickupHandler(),
                new EncounterHandler());
    }

    public GameController(CombatSystem combatSystem, PersistenceManager persistenceManager) {
        this(combatSystem, persistenceManager,
                new InteractionHandler(), new FloorTransitionHandler(),
                new EnemyDropHandler(), new ItemPickupHandler(),
                new EncounterHandler());
    }

    public GameController(CombatSystem combatSystem, PersistenceManager persistenceManager,
                          InteractionHandler interactionHandler, FloorTransitionHandler floorTransitionHandler,
                          EnemyDropHandler enemyDropHandler, ItemPickupHandler itemPickupHandler,
                          EncounterHandler encounterHandler) {
        this.combatSystem = combatSystem;
        this.persistenceManager = persistenceManager;
        this.interactionHandler = interactionHandler;
        this.floorTransitionHandler = floorTransitionHandler;
        this.enemyDropHandler = enemyDropHandler;
        this.itemPickupHandler = itemPickupHandler;
        this.encounterHandler = encounterHandler;
    }

    public void setGamePanel(GameView gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void restartGame() {
        player = CastleFactory.createPlayer();
        castle = CastleFactory.createCastle();
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
        if (!isGameActive()) {
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
        if (!isGameActive() || currentEnemy != null) return;

        int nextX = player.getX() + dx;
        int nextY = player.getY() + dy;

        if (dx != 0 || dy != 0) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
            player.setRotation(angle);
            player.updateWalkPhase(0.3); // Progress animation
        }

        Floor currentFloor = castle.getCurrentFloor();
        if (currentFloor != null) {
            FloorTransitionHandler.TransitionResult transition =
                    floorTransitionHandler.checkExitDoorTransition(player, castle, nextX, nextY, gamePanel);

            if (transition == FloorTransitionHandler.TransitionResult.VICTORY) {
                gameOver = true;
                return;
            } else if (transition == FloorTransitionHandler.TransitionResult.CONTINUE) {
                continueExploring();
                return;
            }

            if (currentFloor.isWalkable(nextX, nextY)) {
                player.move(dx, dy);
                itemPickupHandler.checkItemPickups(player, currentFloor, gamePanel);
                checkEncounters();
                refreshView();
            }
        }
    }

    /**
     * Called on every timer tick to advance door animations and refresh the view.
     */
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
        for (model.Door door : currentFloor.getDoors()) {
            double progress = door.getOpenProgress();
            if (door.isOpen() && progress < 1.0) return true;
            if (!door.isOpen() && progress > 0.0) return true;
        }
        return false;
    }

    public void interact() {
        if (gameOver || currentEnemy != null) return;
        interactionHandler.handleInteraction(player, castle, gamePanel);
    }

    public void continueExploring() {
        if (gameOver || currentEnemy != null) {
            return;
        }
        floorTransitionHandler.continueExploring(player, castle, gamePanel);
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

    private boolean isGameActive() {
        return !gameOver && player != null && player.isAlive();
    }

    private boolean canFight() {
        return isGameActive() && currentEnemy != null && currentEnemy.isAlive();
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
        currentEnemy = enemyDropHandler.handleEnemyDefeated(currentEnemy, player, castle, gamePanel);
    }

    private void checkEncounters() {
        Enemy encountered = encounterHandler.checkEncounters(player, castle, gamePanel);
        if (encountered != null) {
            currentEnemy = encountered;
        }
    }

    private void failMission() {
        gameOver = true;
        currentEnemy = null;
        gamePanel.appendLog("Mission failed. The astronaut has fallen in the fortress.");

        gamePanel.showGameOverDialog(false);

    }

    private void refreshView() {
        gamePanel.refresh();
    }

    public void saveGame(String filepath) throws java.io.IOException {
        String logText = (gamePanel != null) ? gamePanel.getLogText() : "";
        GameState state = new GameState(player, castle, currentEnemy, gameOver, logText);
        persistenceManager.save(state, filepath);
    }

    /**
     * @throws ClassNotFoundException if the save file was created with an incompatible version of the game
     * @throws IOException if the file cannot be read
     */
    public void loadGame(String filepath) throws java.io.IOException, ClassNotFoundException {
        GameState state = persistenceManager.load(filepath);
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
