package com.wormfarm.farm.insect;

import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.farm.resource.Beehive;

import java.util.List;

public class Bee extends FarmInsect {
    public enum BeeState {
        TO_PLANT,
        WITH_NECTAR_TO_HIVE,
        IDLE
    }

    private BeeState beeState = BeeState.IDLE;
    private final Beehive beehive;
    private PlantInstance targetPlant;
    private boolean hasNectar = false;

    private double visualDirectionRad = 0.0;
    private double ellipseProgress = 1.0;

    private static final double PROGRESS_SPEED = 0.002;

    public Bee(int startX, int startY, Beehive beehive, int speed) {
        super("Bee", startX, startY, speed);
        this.beehive = beehive;
        this.x = startX;
        this.y = startY;
    }

    public void assignToPlant(PlantInstance plant) {
        this.targetPlant = plant;
        if (plant != null && plant.getPriceMultiplier() == 1.0) {
            plant.setPriceMultiplier(2.0);
        }
        beeState = BeeState.TO_PLANT;
        ellipseProgress = 0.0;
        hasNectar = false;
    }

    public BeeState getBeeState() { return beeState; }
    public boolean isCarryingNectar() { return hasNectar; }
    public PlantInstance getTargetPlant() { return targetPlant; }
    public double getVisualDirectionRad() { return visualDirectionRad; }
    public double getEllipseProgress() { return ellipseProgress; }

    @Override
    public void tick() {
        if (targetPlant == null || targetPlant.getPriceMultiplier() != 2.0) {
            PlantInstance plant = findFreePlant();
            if (plant != null) assignToPlant(plant);
        }

        switch (beeState) {
            case WITH_NECTAR_TO_HIVE-> {
                if (targetPlant == null) break;
                boolean arrived = moveEllipseTrajectory(beehive.getX(), beehive.getY(), targetPlant.getX(), targetPlant.getY());
                if (arrived) {
                    beeState = BeeState.TO_PLANT;
                    hasNectar = false;
                    ellipseProgress = 0.0;
                }
            }
            case TO_PLANT -> {
                boolean arrived = moveEllipseTrajectory(targetPlant.getX(), targetPlant.getY(), beehive.getX(), beehive.getY());
                if (arrived) {
                    beeState = BeeState.WITH_NECTAR_TO_HIVE;
                    hasNectar = true;
                    ellipseProgress = 0.0;
                }
            }
            case IDLE -> {
                PlantInstance plant = findFreePlant();
                if (plant != null) assignToPlant(plant);
            }
        }
    }

    // Эллипс между from и to, прогресс ellipseProgress от 0 до 1
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
        double b = Math.max(24, a / 4.0);

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
        if (beehive.getFarmController() == null) return null;
        List<PlantInstance> plants = beehive.getFarmController().getPlantField().getPlants();
        for (PlantInstance plant : plants) {
            if (plant.getPriceMultiplier() == 1.0) return plant;
        }
        return null;
    }

    // --- Для восстановления состояния при загрузке ---
    public void setBeeState(BeeState beeState) { this.beeState = beeState; }
    public void setHasNectar(boolean hasNectar) { this.hasNectar = hasNectar; }
    public void setEllipseProgress(double progress) { this.ellipseProgress = progress; }
    public void setVisualDirectionRad(double rad) { this.visualDirectionRad = rad; }
    public void setTargetPlant(PlantInstance plant) { this.targetPlant = plant; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}