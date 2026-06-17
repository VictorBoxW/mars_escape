package model;

public class Key extends Item {
    private static final long serialVersionUID = 1L;
    private final int floorLevel;

    public Key(int floorLevel) {
        super("Access Key - Floor " + floorLevel, "A glowing key used to unlock the floor exit.");
        this.floorLevel = floorLevel;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    @Override
    public String interact(Player player) {
        return "This key grants access to the next floor of the fortress.";
    }
}
