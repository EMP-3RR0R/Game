package com.wormfarm.core.model;

import java.io.Serializable;

public class DungBeetleData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x, y;
    private final String beetleState;
    private final Integer assignedPlantIndex;
    private final boolean hasBall; // <-- обязательно!

    public DungBeetleData(int x, int y, String beetleState, Integer assignedPlantIndex, boolean hasBall) {
        this.x = x;
        this.y = y;
        this.beetleState = beetleState;
        this.assignedPlantIndex = assignedPlantIndex;
        this.hasBall = hasBall;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getBeetleState() { return beetleState; }
    public Integer getAssignedPlantIndex() { return assignedPlantIndex; }
    public boolean hasBall() { return hasBall; }
}