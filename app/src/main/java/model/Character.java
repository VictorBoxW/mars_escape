package model;

public abstract class Character implements Fightable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final int maxHealth;
    private int health;
    private int shield;
    private final int attack;
    private final int defense;


    //Defense is optional and defaults to 0
    protected Character(String name, int maxHealth, int attack) {
        this(name, maxHealth, attack, 0);
    }

    protected Character(String name, int maxHealth, int attack, int defense) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }

        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.shield = 0;
        this.attack = attack;
        this.defense = defense;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHealth() {
        return health;
    }

    public int getShield() {
        return shield;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public int getAttackPower() {
        return attack;
    }

    @Override
    public int getDefensePower() {
        return defense;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public int attackTarget(Fightable target) {
        return target.takeDamage(getAttackPower());
    }

    @Override
    public int takeDamage(int incomingAttack) {
        int damage = Math.max(1, incomingAttack - getDefensePower());
        int remainingDamage = absorbWithShield(damage);
        health = Math.max(0, health - remainingDamage);
        return damage;
    }

    private int absorbWithShield(int damage) {
        if (shield <= 0) return damage;
        if (shield >= damage) { shield -= damage; return 0; }
        damage -= shield;
        shield = 0;
        return damage;
    }

    public int heal(int amount) {
        if (amount <= 0 || !isAlive()) {
            return 0;
        }

        int before = health;
        health = Math.min(maxHealth, health + amount);
        return health - before;
    }

    public void addShield(int amount) {
        if (amount > 0 && isAlive()) {
            this.shield += amount;
        }
    }
}
