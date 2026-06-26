package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoorTest {

    @Test
    void testDoorLocking() {
        Door door = new Door(100, 100, 50, 50, true, false);
        assertTrue(door.isLocked());
        
        door.setLocked(false);
        assertFalse(door.isLocked());
    }

    @Test
    void testDoorOpening() {
        Door door = new Door(100, 100, 50, 50);
        assertFalse(door.isOpen());
        assertEquals(0.0, door.getOpenProgress());
        
        door.setOpen(true);
        assertTrue(door.isOpen());
        
        // Test animation progress update
        door.update();
        assertTrue(door.getOpenProgress() > 0.0);
        
        for(int i = 0; i < 10; i++) door.update();
        assertEquals(1.0, door.getOpenProgress());
        
        door.setOpen(false);
        door.update();
        assertTrue(door.getOpenProgress() < 1.0);
    }

    @Test
    void testProximity() {
        Door door = new Door(100, 100, 50, 50); // Center at 125, 125
        assertTrue(door.isNear(130, 130, 20));
        assertFalse(door.isNear(200, 200, 20));
    }
}
