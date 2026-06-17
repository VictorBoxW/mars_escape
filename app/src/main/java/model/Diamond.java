package model;

public class Diamond extends Item {
    private static final long serialVersionUID = 1L;
    public Diamond() {
        super("Energy Diamond", "The core power source of the fortress. Extremely valuable.");
    }

    @Override
    public String interact(Player player) {
        return "The diamond glows with immense power.";
    }
}
