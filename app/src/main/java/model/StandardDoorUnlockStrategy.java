package model;

/**
 * Standard unlock strategy that requires a key matching the specified floor level.
 */
public class StandardDoorUnlockStrategy implements DoorUnlockStrategy {
    private static final long serialVersionUID = 1L;
    private final int floorLevel;

    public StandardDoorUnlockStrategy(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    private Key findKey(Player player) {
        return player.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.KEY)
                .map(i -> (Key) i)
                .filter(k -> k.getFloorLevel() == floorLevel)
                .findFirst().orElse(null);
    }

    @Override
    public boolean isConditionMet(Player player) {
        return findKey(player) != null;
    }

    @Override
    public void unlock(Player player, Door door) {
        Key key = findKey(player);
        if (key != null) {
            door.setLocked(false);
            door.setOpen(true);
            player.removeItem(key);
        }
    }

    @Override
    public String getUnlockMessage() {
        return "System: Door unlocked using the Access Key! Key consumed.";
    }

    @Override
    public String getFailureMessage() {
        return "System: You have to obtain the key in order to open the door.";
    }

    @Override
    public String getPromptText(Player player) {
        if (isConditionMet(player)) {
            return "Press 'O' to Unlock Door";
        } else {
            return "You have to obtain the key\nin order to open the door.";
        }
    }
}
