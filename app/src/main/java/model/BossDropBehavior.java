package model;

import java.util.function.Consumer;

/**
 * Boss drop behavior that spawns both a floor level key and an Energy Diamond.
 */
public class BossDropBehavior implements FloorClearedDropBehavior {
    private static final long serialVersionUID = 1L;
    private final int floorLevel;

    public BossDropBehavior(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    @Override
    public void spawnDrops(Floor floor, Player player, Consumer<String> logConsumer) {
        Key key = new Key(floorLevel);
        Diamond diamond = new Diamond();
        floor.addItem(new PickableItem(key, player.getX() + 40, player.getY()));
        floor.addItem(new PickableItem(diamond, player.getX() - 40, player.getY()));
        logConsumer.accept("System: The Dark Alien has dropped the fuel key and the ENERGY DIAMOND!");
    }
}
