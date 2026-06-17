package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Castle implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Floor> floors;
    private int currentFloorIndex;
    private boolean energyCrystalTaken;

    public Castle(List<Floor> floors) {
        this.floors = new ArrayList<>(floors);
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public Floor getCurrentFloor() {
        if (currentFloorIndex >= floors.size()) {
            return null;
        }

        return floors.get(currentFloorIndex);
    }

    public int getCurrentFloorNumber() {
        return Math.min(currentFloorIndex + 1, floors.size());
    }

    public int getFloorCount() {
        return floors.size();
    }

    public boolean advanceFloor() {
        if (currentFloorIndex < floors.size()) {
            currentFloorIndex++;
        }

        return currentFloorIndex < floors.size();
    }

    public void takeEnergyCrystal() {
        energyCrystalTaken = true;
    }

    public boolean isEnergyCrystalTaken() {
        return energyCrystalTaken;
    }
}
