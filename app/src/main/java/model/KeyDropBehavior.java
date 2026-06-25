package model;

import java.util.function.Consumer;

/**
 * Standard drop behavior that spawns an access key for the specified floor level.
 */
public class KeyDropBehavior implements FloorClearedDropBehavior {
    private static final long serialVersionUID = 1L;
    private final int floorLevel;

    public KeyDropBehavior(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    @Override
    public void spawnDrops(Floor floor, Player player, Consumer<String> logConsumer) {
        Key key = new Key(floorLevel);
        floor.addItem(new PickableItem(key, player.getX() + 40, player.getY()));
        logConsumer.accept("System: The final guardian has dropped a glowing Access Key!");
    }
}
