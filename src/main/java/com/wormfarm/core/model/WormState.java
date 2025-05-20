package com.wormfarm.core.model;

import java.io.Serializable;

public class WormState implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x, y, direction;
    private int targetX, targetY; // <-- добавлено для сохранения таргета

    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 800;

    public WormState(double x, double y, double direction) {
        this.x = clamp(x, 0, FIELD_WIDTH);
        this.y = clamp(y, 0, FIELD_HEIGHT);
        this.direction = direction;
        this.targetX = (int) this.x; // инициализируем в текущие координаты
        this.targetY = (int) this.y;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = clamp(x, 0, FIELD_WIDTH); }

    public double getY() { return y; }
    public void setY(double y) { this.y = clamp(y, 0, FIELD_HEIGHT); }

    public double getDirection() { return direction; }
    public void setDirection(double direction) { this.direction = direction; }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    // Таргет
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public void setTarget(int targetX, int targetY) {
        this.targetX = clampInt(targetX, 0, FIELD_WIDTH);
        this.targetY = clampInt(targetY, 0, FIELD_HEIGHT);
    }
    private static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    // Обновление всех полей
    public void copyFrom(WormState other) {
        this.x = other.x;
        this.y = other.y;
        this.direction = other.direction;
        this.targetX = other.targetX;
        this.targetY = other.targetY;
    }
}