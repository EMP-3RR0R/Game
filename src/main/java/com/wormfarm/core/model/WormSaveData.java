package com.wormfarm.core.model;

import java.io.Serializable;

public class WormSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final WormState state;
    private final WormStats stats;

    // Новые поля
    private final int targetX;
    private final int targetY;

    public WormSaveData(WormState state, WormStats stats, int targetX, int targetY) {
        this.state = new WormState(state.getX(), state.getY(), state.getDirection());
        this.stats = new WormStats(stats.getWormCoins());
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void applyTo(WormState state, WormStats stats, TargetConsumer targetConsumer) {
        state.setX(this.state.getX());
        state.setY(this.state.getY());
        state.setDirection(this.state.getDirection());
        stats.setWormCoins(this.stats.getWormCoins());
        if (targetConsumer != null) {
            targetConsumer.setTarget(targetX, targetY);
        }
    }

    // Для обратной совместимости
    public void applyTo(WormState state, WormStats stats) {
        applyTo(state, stats, null);
    }

    // Геттеры для targetX/Y если нужно
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }

    public interface TargetConsumer {
        void setTarget(int x, int y);
    }
}