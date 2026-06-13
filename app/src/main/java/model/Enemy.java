package model;

public class Enemy extends Character {
    private final String taunt;
    private final boolean isBoss;

    public Enemy(String name, int maxHealth, int attack, int defense, String taunt) {
        this(name, maxHealth, attack, defense, taunt, false);
    }

    public Enemy(String name, int maxHealth, int attack, int defense, String taunt, boolean isBoss) {
        super(name, maxHealth, attack, defense);
        this.taunt = taunt;
        this.isBoss = isBoss;
    }

    public String getTaunt() {
        return taunt;
    }

    public boolean isBoss() {
        return isBoss;
    }
}
