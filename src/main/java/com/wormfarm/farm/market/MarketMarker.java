package com.wormfarm.farm.market;

public class MarketMarker {
    private final int x;
    private final int y;
    private final int size = 20;

    public MarketMarker(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }

    public boolean contains(int px, int py) {
        int half = size / 2;
        return px >= x - half && px <= x + half && py >= y - half && py <= y + half;
    }
}