package model;

public class Weapon extends Item {
    private final int attackBonus;

    public Weapon(String name, String description, int attackBonus) {
        super(name, description);
        this.attackBonus = attackBonus;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    @Override
    public String interact(Player player) {
        player.equipWeapon(this);
        return player.getName() + " equipped " + getName() + " (+" + attackBonus + " attack).";
    }
}
