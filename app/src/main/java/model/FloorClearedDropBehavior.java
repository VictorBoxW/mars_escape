package model;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Strategy interface to define drop behavior when a Floor is cleared.
 */
public interface FloorClearedDropBehavior extends Serializable {
    void spawnDrops(Floor floor, Player player, Consumer<String> logConsumer);
}
