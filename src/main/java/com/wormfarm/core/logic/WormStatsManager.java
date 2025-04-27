package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormStats;

public class WormStatsManager {
    private final WormStats stats;

    public WormStatsManager(WormStats stats) {
        this.stats = stats;
    }

    public void addCoins(int amount) {
        if (amount > 0) {
            stats.setWormCoins(stats.getWormCoins() + amount);
        }
    }

    public boolean spendCoins(int amount) {
        if (amount > 0 && stats.getWormCoins() >= amount) {
            stats.setWormCoins(stats.getWormCoins() - amount);
            return true;
        }
        return false;
    }

    public int getCoins() {
        return stats.getWormCoins();
    }

    // --- Геттер для сохранения ---
    public WormStats getStats() {
        return stats;
    }
}