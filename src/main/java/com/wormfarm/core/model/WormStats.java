package com.wormfarm.core.model;

import java.io.Serializable;

public class WormStats implements Serializable {
    private static final long serialVersionUID = 1L;

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