package model;

public class Enemy extends Character {
    private static final long serialVersionUID = 1L;
    private final String taunt;
    private final boolean isBoss;

    public Enemy(String name, int maxHealth, int attack, String taunt) {
        this(name, maxHealth, attack, taunt, false);
    }

    public Enemy(String name, int maxHealth, int attack, String taunt, boolean isBoss) {
        super(name, maxHealth, attack);
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
