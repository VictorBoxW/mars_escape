package model;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Castle castle;
    private final Enemy currentEnemy;
    private final boolean gameOver;
    private final String logHistory;

    public GameState(Player player, Castle castle, Enemy currentEnemy, boolean gameOver, String logHistory) {
        this.player = player;
        this.castle = castle;
        this.currentEnemy = currentEnemy;
        this.gameOver = gameOver;
        this.logHistory = logHistory;
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

    public String getLogHistory() {
        return logHistory;
    }
}
