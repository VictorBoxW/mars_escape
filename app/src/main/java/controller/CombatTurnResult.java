package controller;

public class CombatTurnResult {
    private final String message;
    private final boolean enemyDefeated;
    private final boolean playerDefeated;

    public CombatTurnResult(String message, boolean enemyDefeated, boolean playerDefeated) {
        this.message = message;
        this.enemyDefeated = enemyDefeated;
        this.playerDefeated = playerDefeated;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnemyDefeated() {
        return enemyDefeated;
    }

    public boolean isPlayerDefeated() {
        return playerDefeated;
    }
}
