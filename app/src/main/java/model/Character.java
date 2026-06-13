package model;

public abstract class Character implements Fightable {
    private final String name;
    private final int maxHealth;
    private int health;
    private int shield;
    private final int attack;
    private final int defense;

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
        
        if (shield > 0) {
            if (shield >= damage) {
                shield -= damage;
                return damage;
            } else {
                damage -= shield;
                shield = 0;
            }
        }
        
        health = Math.max(0, health - damage);
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
