package com.wormfarm.farm.resource;

import com.wormfarm.farm.FarmController;

public class Beehive {
    private final int x, y;
    private final int radius;
    private FarmController farmController;

    public Beehive(int x, int y, int diameter) {
        this.x = x;
        this.y = y;
        this.radius = diameter / 2;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }
    public void setFarmController(FarmController fc) { this.farmController = fc; }
    public FarmController getFarmController() { return farmController; }
}