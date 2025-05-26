package com.wormfarm.core.model;

import java.io.Serializable;

public class AntData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x, y;
    private final String antState;
    private final Integer assignedPlantIndex;
    private final boolean hasAphid;
    private final double ellipseProgress;
    private final double visualDirectionRad;

    public AntData(int x, int y, String antState, Integer assignedPlantIndex, boolean hasAphid, double ellipseProgress, double visualDirectionRad) {
        this.x = x;
        this.y = y;
        this.antState = antState;
        this.assignedPlantIndex = assignedPlantIndex;
        this.hasAphid = hasAphid;
        this.ellipseProgress = ellipseProgress;
        this.visualDirectionRad = visualDirectionRad;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getAntState() { return antState; }
    public Integer getAssignedPlantIndex() { return assignedPlantIndex; }
    public boolean hasAphid() { return hasAphid; }
    public double getEllipseProgress() { return ellipseProgress; }
    public double getVisualDirectionRad() { return visualDirectionRad; }
}