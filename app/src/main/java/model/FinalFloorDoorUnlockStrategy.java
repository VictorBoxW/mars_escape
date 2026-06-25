package model;

/**
 * Final floor unlock strategy that requires both the key of the floor level and the Energy Diamond.
 */
public class FinalFloorDoorUnlockStrategy implements DoorUnlockStrategy {
    private static final long serialVersionUID = 1L;
    private final int floorLevel;

    public FinalFloorDoorUnlockStrategy(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    private Key findKey(Player player) {
        return player.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.KEY)
                .map(i -> (Key) i)
                .filter(k -> k.getFloorLevel() == floorLevel)
                .findFirst().orElse(null);
    }

    private Item findDiamond(Player player) {
        return player.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.DIAMOND)
                .findFirst().orElse(null);
    }

    @Override
    public boolean isConditionMet(Player player) {
        return findKey(player) != null && findDiamond(player) != null;
    }

    @Override
    public void unlock(Player player, Door door) {
        Key key = findKey(player);
        Item diamond = findDiamond(player);
        if (key != null && diamond != null) {
            door.setLocked(false);
            door.setOpen(true);
            player.removeItem(key);
            player.removeItem(diamond);
        }
    }

    @Override
    public String getUnlockMessage() {
        return "System: Exit Door unlocked using the Diamond and Key! Items consumed.";
    }

    @Override
    public String getFailureMessage() {
        return "System: You have to obtain both the Key and the Diamond to open this door.";
    }

    @Override
    public String getPromptText(Player player) {
        if (isConditionMet(player)) {
            return "Press 'O' to Unlock Door";
        } else {
            return "You have to obtain both the Key\nand the Diamond to open this door.";
        }
    }
}
