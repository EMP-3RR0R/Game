package com.wormfarm.farm.insect;

import java.io.Serializable;
import java.util.UUID;

public abstract class FarmInsect implements Serializable {
    protected final String id;      // Уникальный идентификатор
    protected String name;          // Имя или тип насекомого
    protected int x, y;             // Положение на карте
    protected int speed;            // Скорость перемещения
    protected State state;          // Текущее состояние
    protected Integer targetX;      // Цель по X (может быть null)
    protected Integer targetY;      // Цель по Y (может быть null)

    public enum State {
        IDLE,           // Ожидание
        MOVING,         // В движении к цели
        WORKING,        // Выполняет задание
        CUSTOM          // Для расширения
    }

    public FarmInsect(String name, int startX, int startY, int speed) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.state = State.IDLE;
        this.targetX = null;
        this.targetY = null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSpeed() { return speed; }
    public State getState() { return state; }

    public Integer getTargetX() { return targetX; }
    public Integer getTargetY() { return targetY; }
    public void setTarget(Integer x, Integer y) { this.targetX = x; this.targetY = y; }

    public void tick() {
        switch (state) {
            case IDLE -> onIdle();
            case MOVING -> moveToTarget();
            case WORKING -> onWorking();
            case CUSTOM -> onCustom();
        }
    }

    protected void onIdle() {
        // По умолчанию ничего не делает
    }

    protected void moveToTarget() {
        if (targetX == null || targetY == null) {
            state = State.IDLE;
            return;
        }
        if (x != targetX) x += Integer.compare(targetX, x) * Math.min(speed, Math.abs(targetX - x));
        if (y != targetY) y += Integer.compare(targetY, y) * Math.min(speed, Math.abs(targetY - y));
        if (x == targetX && y == targetY) {
            onArrived();
        }
    }

    protected void onArrived() {
        // По умолчанию становится WORKING
        state = State.WORKING;
    }

    protected void onWorking() {
        // По умолчанию ничего не делает
    }

    protected void onCustom() {
        // Для расширения в наследниках
    }

    public void goTo(int x, int y) {
        setTarget(x, y);
        state = State.MOVING;
    }

    public String serialize() {
        return id + "|" + name + "|" + x + "|" + y + "|" + speed + "|" + state +
                "|" + (targetX != null ? targetX : "null") + "|" + (targetY != null ? targetY : "null");
    }

    @Override
    public String toString() {
        return "FarmInsect{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", speed=" + speed +
                ", state=" + state +
                ", targetX=" + targetX +
                ", targetY=" + targetY +
                '}';
    }
}