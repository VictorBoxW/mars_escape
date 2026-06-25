package model;

public class Consumable extends Item {
    private static final long serialVersionUID = 1L;
    private final int healingAmount;

    public Consumable(String name, String description, int healingAmount) {
        super(name, description);
        this.healingAmount = healingAmount;
    }

    public int getHealingAmount() {
        return healingAmount;
    }

    @Override
    public String interact(Player player) {
        int healed = player.heal(healingAmount);
        player.removeItem(this);

        if (healed == 0) {
            return getName() + " had no effect.";
        }

        return player.getName() + " used " + getName() + " and recovered " + healed + " health.";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.CONSUMABLE;
    }
}
