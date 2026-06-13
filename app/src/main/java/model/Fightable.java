package model;

public interface Fightable {
    String getName();

    int getHealth();

    int getMaxHealth();

    int getAttackPower();

    int getDefensePower();

    boolean isAlive();

    int attackTarget(Fightable target);

    int takeDamage(int incomingAttack);
}
