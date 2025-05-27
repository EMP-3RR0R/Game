package com.wormfarm.farm.insect;

import java.io.Serializable;
import java.util.UUID;

public abstract class FarmInsect implements Serializable {
    protected final String id;
    protected String name;
    protected int x, y;
    protected int speed;
    protected State state;
    protected Integer targetX;
    protected Integer targetY;

    public enum State {
        IDLE,
        MOVING,
        WORKING,
        CUSTOM
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
        state = State.WORKING;
    }

    protected void onWorking() {
    }

    protected void onCustom() {
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