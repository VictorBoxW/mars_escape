package model;

public class ShieldItem extends Item {
    private static final long serialVersionUID = 1L;
    private final int shieldAmount;

    public ShieldItem(String name, String description, int shieldAmount) {
        super(name, description);
        this.shieldAmount = shieldAmount;
    }

    @Override
    public String interact(Player player) {
        player.addShield(shieldAmount);
        player.removeItem(this);
        return player.getName() + " activated " + getName() + " and gained " + shieldAmount + " shield.";
    }
}
