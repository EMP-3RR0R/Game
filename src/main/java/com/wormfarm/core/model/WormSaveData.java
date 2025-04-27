package com.wormfarm.core.model;

import java.io.Serializable;

public class WormSaveData implements Serializable {
    public double x, y, direction;
    public int wormCoins;

    public WormSaveData() {}

    public WormSaveData(WormState state, WormStats stats) {
        this.x = state.getX();
        this.y = state.getY();
        this.direction = state.getDirection();
        this.wormCoins = stats.getWormCoins();
    }

    public void applyTo(WormState state, WormStats stats) {
        state.setX(x);
        state.setY(y);
        state.setDirection(direction);
        stats.setWormCoins(wormCoins);
    }
}