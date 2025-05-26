package com.wormfarm.farm.plant;

import com.wormfarm.farm.insect.DungBeetle;

public class PlantInstance implements PriceUpgradable {
    private final int x;
    private final int y;
    private long plantedAtMillis;
    private long growthAccumulatedMillis = 0;
    private boolean paused = false;
    private DungBeetle assignedBeetle = null;

    // Для корректной работы глобальной паузы
    private long pauseStartedAt = 0;

    // Для поддержки апгрейда цены и скорости роста (пчёлы, муравьи)
    private double priceMultiplier = 1.0;
    private double growthSpeedMultiplier = 1.0;

    private double beetleGrowthMultiplier = 1.0; // Сохраняем multiplier от навозника

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
            if (beetle != null) {
                beetleGrowthMultiplier = beetle.getGrowthMultiplier();
            }
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
                growthAccumulatedMillis += getUnpausedDelta(currentTimeMillis);
            } else {
                plantedAtMillis = currentTimeMillis;
            }
            this.paused = paused;
        }
    }

    private long getUnpausedDelta(long currentTimeMillis) {
        return currentTimeMillis - plantedAtMillis;
    }

    public boolean isPaused() {
        return paused;
    }

    // Глобальная пауза (для всей фермы)
    public void setGlobalPaused(boolean paused, long currentTimeMillis) {
        if (paused) {
            if (!this.paused) {
                // Уходим на паузу: накапливаем всё текущее время
                growthAccumulatedMillis += getUnpausedDelta(currentTimeMillis);
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

    /// ВАЖНО: тут теперь применяется общий множитель ускорения!
    public long getGrowthMillis(long currentTimeMillis) {
        long total = growthAccumulatedMillis;
        if (!paused) {
            total += getUnpausedDelta(currentTimeMillis);
        }
        double multiplier = getTotalGrowthMultiplier();
        long adjusted = (long) (total * multiplier);
        System.out.println("[DEBUG] PlantInstance.getGrowthMillis: x=" + x + " y=" + y + " total=" + total + " adj=" + adjusted + " paused=" + paused + " multiplier=" + multiplier);
        return adjusted;
    }

    // Ускорение: множитель навозника * множитель муравья/пчелы (growthSpeedMultiplier)
    public double getTotalGrowthMultiplier() {
        double m = 1.0;
        if (assignedBeetle != null) {
            m *= beetleGrowthMultiplier;
        }
        m *= growthSpeedMultiplier;
        return m;
    }

    // boostGrowth теперь НЕ нужен, но можно оставить для совместимости
    public void boostGrowth(double multiplier) {
        // Не нужен, ускорение реализовано через getGrowthMillis
    }

    public void setGrowthAccumulatedMillis(long ms) {
        this.growthAccumulatedMillis = ms;
        System.out.println("[DEBUG] PlantInstance.setGrowthAccumulatedMillis: x=" + x + " y=" + y + " ms=" + ms);
    }

    // --- PriceUpgradable реализация ---
    @Override
    public void setPriceMultiplier(double mul) {
        this.priceMultiplier = mul;
    }

    @Override
    public void setGrowthSpeedMultiplier(double mul) {
        this.growthSpeedMultiplier = mul;
    }

    @Override
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public double getGrowthSpeedMultiplier() {
        return growthSpeedMultiplier;
    }
}