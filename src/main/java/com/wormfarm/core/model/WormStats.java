package com.wormfarm.core.model;

public class WormStats {
    private int wormCoins;

    public WormStats(int initialCoins) {
        this.wormCoins = initialCoins;
    }

    public int getWormCoins() {
        return wormCoins;
    }

    public void setWormCoins(int wormCoins) {
        this.wormCoins = wormCoins;
    }
}