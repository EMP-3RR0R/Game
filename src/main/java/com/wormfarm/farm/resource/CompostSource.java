package com.wormfarm.farm.resource;

public class CompostSource {
    private final int x;
    private final int y;
    private final int radius;

    public CompostSource(int x, int y, int diameter) {
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
}