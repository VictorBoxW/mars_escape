package controller;

import java.util.Random;
import model.Enemy;
import model.Player;

public class CombatSystem {
    private final Random random;

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
            message.append(System.lineSeparator()).append(enemyTurn(player, enemy, false));
        } else {
            message.append(System.lineSeparator()).append(enemy.getName()).append(" is defeated.");
        }

        return new CombatTurnResult(message.toString(), !enemy.isAlive(), !player.isAlive());
    }

    public CombatTurnResult dodge(Player player, Enemy enemy) {
        StringBuilder message = new StringBuilder();
        message.append(player.getName()).append(" braces and tries to dodge.");
        message.append(System.lineSeparator()).append(enemyTurn(player, enemy, true));

        return new CombatTurnResult(message.toString(), !enemy.isAlive(), !player.isAlive());
    }

    public CombatTurnResult useItem(Player player, Enemy enemy, int itemIndex) {
        StringBuilder message = new StringBuilder(player.useItem(itemIndex));

        if (enemy != null && enemy.isAlive()) {
            message.append(System.lineSeparator()).append(enemyTurn(player, enemy, false));
        }

        return new CombatTurnResult(message.toString(), enemy != null && !enemy.isAlive(), !player.isAlive());
    }

    private String enemyTurn(Player player, Enemy enemy, boolean playerDodging) {
        if (playerDodging && random.nextInt(100) < 60) {
            return player.getName() + " dodged " + enemy.getName() + "'s attack.";
        }

        int attackPower = enemy.getAttackPower();
        String hitType = "hits";
        if (playerDodging) {
            attackPower = Math.max(1, attackPower / 2);
            hitType = "clips";
        }

        int damage = player.takeDamage(attackPower);
        return enemy.getName() + " " + hitType + " " + player.getName() + " for " + damage + " damage.";
    }
}
