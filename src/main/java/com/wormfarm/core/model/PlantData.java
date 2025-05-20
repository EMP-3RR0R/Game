package com.wormfarm.core.model;

import java.io.Serializable;

public class PlantData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x, y;
    private final long plantedAtMillis;
    private final long growthAccumulatedMillis;
    private final boolean paused;

    public PlantData(int x, int y, long plantedAtMillis, long growthAccumulatedMillis, boolean paused) {
        this.x = x;
        this.y = y;
        this.plantedAtMillis = plantedAtMillis;
        this.growthAccumulatedMillis = growthAccumulatedMillis;
        this.paused = paused;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public long getPlantedAtMillis() { return plantedAtMillis; }
    public long getGrowthAccumulatedMillis() { return growthAccumulatedMillis; }
    public boolean isPaused() { return paused; }
}