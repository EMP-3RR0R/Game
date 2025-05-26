package com.wormfarm.core.model;

import java.io.Serializable;

public class BeeData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x, y;
    private final String beeState;
    private final Integer assignedPlantIndex;
    private final boolean hasNectar;
    private final double ellipseProgress;
    private final double visualDirectionRad;

    public BeeData(int x, int y, String beeState, Integer assignedPlantIndex, boolean hasNectar, double ellipseProgress, double visualDirectionRad) {
        this.x = x;
        this.y = y;
        this.beeState = beeState;
        this.assignedPlantIndex = assignedPlantIndex;
        this.hasNectar = hasNectar;
        this.ellipseProgress = ellipseProgress;
        this.visualDirectionRad = visualDirectionRad;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getBeeState() { return beeState; }
    public Integer getAssignedPlantIndex() { return assignedPlantIndex; }
    public boolean hasNectar() { return hasNectar; }
    public double getEllipseProgress() { return ellipseProgress; }
    public double getVisualDirectionRad() { return visualDirectionRad; }
}