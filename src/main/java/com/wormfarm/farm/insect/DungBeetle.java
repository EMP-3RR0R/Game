package com.wormfarm.farm.insect;

import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.farm.resource.CompostSource;

public class DungBeetle extends FarmInsect {
    public enum BeetleState {
        TO_COMPOST,
        WITH_BALL_TO_PLANT,
        TO_COMPOST_NO_BALL,
        IDLE
    }

    private BeetleState beetleState = BeetleState.IDLE;
    private final CompostSource compostSource;
    public PlantInstance targetPlant; // public для простоты
    private final double growthMultiplier;
    private boolean hasBall = false;

    private final int speedWithBall;
    private final int speedWithoutBall;

    public DungBeetle(int startX, int startY, CompostSource compostSource, int speedWithBall, int speedWithoutBall, double growthMultiplier) {
        super("Dung Beetle", startX, startY, speedWithoutBall);
        this.compostSource = compostSource;
        this.growthMultiplier = growthMultiplier;
        this.speedWithBall = speedWithBall;
        this.speedWithoutBall = speedWithoutBall;
    }

    // Назначение растения — только один раз!
    public void assignToPlant(PlantInstance plant) {
        this.targetPlant = plant;
        if (plant.getAssignedBeetle() == null) {
            plant.setAssignedBeetle(this);
        }
        this.beetleState = BeetleState.TO_COMPOST;
        setTarget(compostSource.getX(), compostSource.getY());
        state = State.MOVING;
    }

    public BeetleState getBeetleState() { return beetleState; }
    public boolean isCarryingBall() { return hasBall; }
    public PlantInstance getTargetPlant() { return targetPlant; }
    public double getGrowthMultiplier() { return growthMultiplier; }

    public void setHasBall(boolean hasBall) { this.hasBall = hasBall; }
    public void setBeetleState(BeetleState beetleState) { this.beetleState = beetleState; }

    @Override
    public void tick() {
        switch (beetleState) {
            case TO_COMPOST -> {
                moveToTarget(speedWithoutBall);
                if (compostSource.contains(x, y)) {
                    hasBall = true;
                    beetleState = BeetleState.WITH_BALL_TO_PLANT;
                    if (targetPlant != null) {
                        setTarget(targetPlant.getX(), targetPlant.getY());
                        state = State.MOVING;
                    } else {
                        beetleState = BeetleState.IDLE;
                        state = State.IDLE;
                    }
                }
            }
            case WITH_BALL_TO_PLANT -> {
                if (targetPlant == null) {
                    beetleState = BeetleState.TO_COMPOST_NO_BALL;
                    hasBall = false;
                    setTarget(compostSource.getX(), compostSource.getY());
                    state = State.MOVING;
                    break;
                }
                moveToTarget(speedWithBall);
                if (x == targetPlant.getX() && y == targetPlant.getY()) {
                    // Просто сбрасываем шар, никаких setAssignedBeetle!
                    hasBall = false;
                    beetleState = BeetleState.TO_COMPOST_NO_BALL;
                    setTarget(compostSource.getX(), compostSource.getY());
                    state = State.MOVING;
                }
            }
            case TO_COMPOST_NO_BALL -> {
                moveToTarget(speedWithoutBall);
                if (compostSource.contains(x, y)) {
                    beetleState = BeetleState.TO_COMPOST;
                    state = State.MOVING;
                }
            }
            case IDLE -> {}
        }
    }

    void moveToTarget(int curSpeed) {
        if (x == targetX && y == targetY) return;
        int dx = targetX - x;
        int dy = targetY - y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        int step = (int)Math.round(Math.min(curSpeed, len));
        if (step == 0) step = 1;
        x += (int)Math.round(dx / len * step);
        y += (int)Math.round(dy / len * step);
        if (Math.abs(x - targetX) < step) x = targetX;
        if (Math.abs(y - targetY) < step) y = targetY;
    }

    public boolean shouldDrawBall() {
        return beetleState == BeetleState.WITH_BALL_TO_PLANT && hasBall;
    }

    public int[] getBallPosition() {
        if (!shouldDrawBall()) return null;
        int dx = targetX - x;
        int dy = targetY - y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return new int[] { x, y };
        int bx = x + (int)(dx / len * 16);
        int by = y + (int)(dy / len * 16);
        return new int[]{bx, by};
    }
}