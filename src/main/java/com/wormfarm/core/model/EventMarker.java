package com.wormfarm.core.model;

public class EventMarker {
    private final int x, y;
    private final String description;

    public EventMarker(int x, int y, String description) {
        this.x = x;
        this.y = y;
        this.description = description;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getDescription() { return description; }
}