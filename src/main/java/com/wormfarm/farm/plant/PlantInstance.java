package com.wormfarm.farm.plant;

import com.wormfarm.farm.insect.DungBeetle;

public class PlantInstance {
    private final int x;
    private final int y;
    private long plantedAtMillis;
    private long growthAccumulatedMillis = 0;
    private boolean paused = false;
    private DungBeetle assignedBeetle = null;

    // Для корректной работы глобальной паузы
    private long pauseStartedAt = 0;

    public PlantInstance(int x, int y, long plantedAtMillis) {
        this.x = x;
        this.y = y;
        this.plantedAtMillis = plantedAtMillis;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public long getPlantedAtMillis() { return plantedAtMillis; }

    public void setAssignedBeetle(DungBeetle beetle) {
        if (this.assignedBeetle == null) {
            this.assignedBeetle = beetle;
            System.out.println("[DEBUG] PlantInstance.setAssignedBeetle: x=" + x + " y=" + y + " beetle=" + beetle);
        }
    }

    public DungBeetle getAssignedBeetle() {
        return assignedBeetle;
    }

    public void setPaused(boolean paused, long currentTimeMillis) {
        if (this.paused != paused) {
            System.out.println("[DEBUG] PlantInstance.setPaused: x=" + x + " y=" + y + " from " + this.paused + " to " + paused + " at " + currentTimeMillis);
            if (paused) {
                growthAccumulatedMillis += currentTimeMillis - plantedAtMillis;
            } else {
                plantedAtMillis = currentTimeMillis;
            }
            this.paused = paused;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    // Глобальная пауза (для всей фермы)
    public void setGlobalPaused(boolean paused, long currentTimeMillis) {
        if (paused) {
            if (!this.paused) {
                // Уходим на паузу: накапливаем всё текущее время
                growthAccumulatedMillis += currentTimeMillis - plantedAtMillis;
                pauseStartedAt = currentTimeMillis;
                this.paused = true;
                System.out.println("[DEBUG] PlantInstance.setGlobalPaused: PAUSE x=" + x + " y=" + y + " accum=" + growthAccumulatedMillis + " at=" + currentTimeMillis);
            }
        } else {
            if (this.paused) {
                // Выходим с паузы: просто сдвигаем plantedAtMillis
                plantedAtMillis = currentTimeMillis;
                pauseStartedAt = 0;
                this.paused = false;
                System.out.println("[DEBUG] PlantInstance.setGlobalPaused: RESUME x=" + x + " y=" + y + " at=" + currentTimeMillis);
            }
        }
    }

    // Проверка: растение созрело (по времени)
    public boolean isReadyToHarvest(long currentTimeMillis) {
        long total = getGrowthMillis(currentTimeMillis);
        boolean ready = total >= 10 * 1000;
        System.out.println("[DEBUG] PlantInstance.isReadyToHarvest: x=" + x + " y=" + y + " total=" + total + " ready=" + ready + " paused=" + paused);
        return ready;
    }

    public void resetGrowth(long currentTimeMillis) {
        System.out.println("[DEBUG] PlantInstance.resetGrowth: x=" + x + " y=" + y + " at " + currentTimeMillis);
        plantedAtMillis = currentTimeMillis;
        growthAccumulatedMillis = 0;
    }

    public long getGrowthMillis(long currentTimeMillis) {
        long total = growthAccumulatedMillis;
        if (!paused) {
            total += currentTimeMillis - plantedAtMillis;
        }
        System.out.println("[DEBUG] PlantInstance.getGrowthMillis: x=" + x + " y=" + y + " total=" + total + " paused=" + paused);
        return total;
    }

    public void boostGrowth(double multiplier) {
        if (multiplier <= 1.0) return;
        long bonus = (long) (multiplier * 10);
        growthAccumulatedMillis += bonus;
        System.out.println("[DEBUG] PlantInstance.boostGrowth: x=" + x + " y=" + y + " multiplier=" + multiplier + " bonus=" + bonus + " newAccumulated=" + growthAccumulatedMillis);
    }

    public void setGrowthAccumulatedMillis(long ms) {
        this.growthAccumulatedMillis = ms;
        System.out.println("[DEBUG] PlantInstance.setGrowthAccumulatedMillis: x=" + x + " y=" + y + " ms=" + ms);
    }
}