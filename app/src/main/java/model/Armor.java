package model;

public class Armor extends Item {
    private final int defenseBonus;

    public Armor(String name, String description, int defenseBonus) {
        super(name, description);
        this.defenseBonus = defenseBonus;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    @Override
    public String interact(Player player) {
        player.equipArmor(this);
        return player.getName() + " equipped " + getName() + " (+" + defenseBonus + " defense).";
    }
}
