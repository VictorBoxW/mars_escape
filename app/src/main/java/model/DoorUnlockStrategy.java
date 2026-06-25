package model;

import java.io.Serializable;

/**
 * Strategy interface to define unlock requirements and execution for doors.
 */
public interface DoorUnlockStrategy extends Serializable {
    boolean isConditionMet(Player player);
    void unlock(Player player, Door door);
    String getUnlockMessage();
    String getFailureMessage();
    String getPromptText(Player player);
}
