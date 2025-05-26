package com.wormfarm.farm.insect;

import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.farm.resource.Anthill;

import java.util.List;

public class Ant extends FarmInsect {

    public enum AntState {
        TO_PLANT,
        WITH_APHID_TO_ANTHILL,
        IDLE
    }

    private AntState antState = AntState.IDLE;
    private final Anthill anthill;
    private PlantInstance targetPlant;
    private boolean hasAphid = false;

    private double visualDirectionRad = 0.0;
    private double ellipseProgress = 1.0;

    private static final double PROGRESS_SPEED = 0.002;

    public Ant(int startX, int startY, Anthill anthill, int speed) {
        super("Ant", startX, startY, speed);
        this.anthill = anthill;
        this.x = startX;
        this.y = startY;
    }

    public void assignToPlant(PlantInstance plant) {
        this.targetPlant = plant;
        if (plant != null && plant.getGrowthSpeedMultiplier() == 1.0) {
            plant.setPriceMultiplier(2.0);
            plant.setGrowthSpeedMultiplier(1.0 / 1.5);
        }
        antState = AntState.TO_PLANT;
        ellipseProgress = 0.0;
        hasAphid = true;
    }

    public AntState getAntState() { return antState; }
    public boolean isCarryingAphid() { return hasAphid; }
    public PlantInstance getTargetPlant() { return targetPlant; }
    public double getVisualDirectionRad() { return visualDirectionRad; }
    public double getEllipseProgress() { return ellipseProgress; }

    @Override
    public void tick() {
        if (targetPlant == null || targetPlant.getGrowthSpeedMultiplier() != (1.0 / 1.5)) {
            PlantInstance plant = findFreePlant();
            if (plant != null) assignToPlant(plant);
        }

        switch (antState) {
            case WITH_APHID_TO_ANTHILL -> {
                if (targetPlant == null) break;
                boolean arrived = moveEllipseTrajectory(anthill.getX(), anthill.getY(), targetPlant.getX(), targetPlant.getY());
                if (arrived) {
                    antState = AntState.TO_PLANT;
                    hasAphid = true;
                    ellipseProgress = 0.0;
                }
            }
            case TO_PLANT -> {
                boolean arrived = moveEllipseTrajectory(targetPlant.getX(), targetPlant.getY(), anthill.getX(), anthill.getY());
                if (arrived) {
                    antState = AntState.WITH_APHID_TO_ANTHILL;
                    hasAphid = false;
                    ellipseProgress = 0.0;
                }
            }
            case IDLE -> {
                PlantInstance plant = findFreePlant();
                if (plant != null) assignToPlant(plant);
            }
        }
    }

    private boolean moveEllipseTrajectory(int fromX, int fromY, int toX, int toY) {
        if (ellipseProgress >= 1.0) {
            x = toX;
            y = toY;
            visualDirectionRad = Math.atan2(toY - fromY, toX - fromX);
            return true;
        }
        double prevX = x, prevY = y;
        ellipseProgress += PROGRESS_SPEED * speed;
        if (ellipseProgress > 1.0) ellipseProgress = 1.0;

        double t = ellipseProgress;
        double cx = (fromX + toX) / 2.0;
        double cy = (fromY + toY) / 2.0;
        double dx = toX - fromX;
        double dy = toY - fromY;
        double len = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);

        double a = len / 2.0;
        double b = Math.max(24, a * 0.6);

        double theta = Math.PI * t;

        double ex = cx + a * Math.cos(theta) * Math.cos(angle) - b * Math.sin(theta) * Math.sin(angle);
        double ey = cy + a * Math.cos(theta) * Math.sin(angle) + b * Math.sin(theta) * Math.cos(angle);

        x = (int) Math.round(ex);
        y = (int) Math.round(ey);

        if (ellipseProgress < 1.0) {
            visualDirectionRad = Math.atan2(y - prevY, x - prevX);
        } else {
            visualDirectionRad = angle;
        }
        return ellipseProgress >= 1.0;
    }

    private PlantInstance findFreePlant() {
        if (anthill.getFarmController() == null) return null;
        List<PlantInstance> plants = anthill.getFarmController().getPlantField().getPlants();
        for (PlantInstance plant : plants) {
            if (plant.getGrowthSpeedMultiplier() == 1.0) return plant;
        }
        return null;
    }

    // --- Для восстановления состояния при загрузке ---
    public void setAntState(AntState antState) { this.antState = antState; }
    public void setHasAphid(boolean hasAphid) { this.hasAphid = hasAphid; }
    public void setEllipseProgress(double progress) { this.ellipseProgress = progress; }
    public void setVisualDirectionRad(double rad) { this.visualDirectionRad = rad; }
    public void setTargetPlant(PlantInstance plant) { this.targetPlant = plant; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}