package controller;

import java.util.Random;

import model.Enemy;
import model.Player;

public class CombatSystem {
    private final Random random;
    private static final int DODGE_SUCCESS_CHANCE = 60;

    private enum PlayerAction {ATTACKING, DODGING}

    public CombatSystem() {
        this(new Random());
    }

    CombatSystem(Random random) {
        this.random = random;
    }

    public CombatTurnResult attack(Player player, Enemy enemy) {
        StringBuilder message = new StringBuilder();
        int damage = player.attackTarget(enemy);
        message.append(player.getName())
                .append(" attacks ")
                .append(enemy.getName())
                .append(" for ")
                .append(damage)
                .append(" damage.");

        if (enemy.isAlive()) {
            message.append(System.lineSeparator()).append(enemyTurn(player, enemy, PlayerAction.ATTACKING));
        } else {
            message.append(System.lineSeparator()).append(enemy.getName()).append(" is defeated.");
        }

        return buildResult(message.toString(), enemy, player);
    }

    public CombatTurnResult dodge(Player player, Enemy enemy) {
        StringBuilder message = new StringBuilder();
        message.append(player.getName()).append(" braces and tries to dodge.");
        message.append(System.lineSeparator()).append(enemyTurn(player, enemy, PlayerAction.DODGING));

        return buildResult(message.toString(), enemy, player);
    }

    public CombatTurnResult useItem(Player player, Enemy enemy, int itemIndex) {
        StringBuilder message = new StringBuilder(player.useItem(itemIndex));

        if (enemy != null && enemy.isAlive()) {
            message.append(System.lineSeparator()).append(enemyTurn(player, enemy, PlayerAction.ATTACKING));
        }

        return buildResult(message.toString(), enemy, player);
    }

    private String enemyTurn(Player player, Enemy enemy, PlayerAction action) {
        if (action == PlayerAction.DODGING && dodgeSucceeds()) {
            return player.getName() + " dodged " + enemy.getName() + "'s attack.";
        }
        return applyEnemyAttack(player, enemy, action);
    }

    private boolean dodgeSucceeds() {
        return random.nextInt(100) < DODGE_SUCCESS_CHANCE;
    }

    private String applyEnemyAttack(Player player, Enemy enemy, PlayerAction action) {
        boolean isDodging = (action == PlayerAction.DODGING);
        int attackPower = isDodging ? Math.max(1, enemy.getAttackPower() / 2) : enemy.getAttackPower();
        String hitType = isDodging ? "clips" : "hits";

        int damage = player.takeDamage(attackPower);
        return enemy.getName() + " " + hitType + " " + player.getName() + " for " + damage + " damage.";
    }

    private CombatTurnResult buildResult(String message, Enemy enemy, Player player) {
        boolean enemyDefeated = (enemy != null) && !enemy.isAlive();
        return new CombatTurnResult(message, enemyDefeated, !player.isAlive());
    }
}