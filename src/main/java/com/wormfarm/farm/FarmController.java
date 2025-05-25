package com.wormfarm.farm;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.farm.plant.PlantField;
import com.wormfarm.farm.market.MarketMarker;
import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.core.model.FarmSaveData;
import com.wormfarm.core.model.PlantData;
import com.wormfarm.core.model.DungBeetleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FarmController {
    private final List<DungBeetle> dungBeetles = new ArrayList<>();
    private final CompostSource compostSource;
    private final PlantField plantField = new PlantField();
    private final MarketMarker marketMarker = new MarketMarker(320, 100);

    private WormStatsManager statsManager;

    // Таймеры для роста и автоурожая (НЕ зависят от paused карты/панели!)
    private final Timer growthTimer = new Timer("PlantGrowthTimer", true);
    private final Timer harvestTimer = new Timer("AutoHarvestTimer", true);

    private volatile boolean globalPaused = false; // только для полной паузы!

    public FarmController(CompostSource compostSource, WormStatsManager statsManager) {
        this.compostSource = compostSource;
        this.statsManager = statsManager;
        startGrowthTimer();
        startHarvestTimer();
    }

    // --- ДОБАВЛЕНО: корректное завершение таймеров ---
    public void shutdown() {
        try {
            growthTimer.cancel();
        } catch (Exception ignored) {}
        try {
            harvestTimer.cancel();
        } catch (Exception ignored) {}
    }

    // Только для полной паузы!
    public void setGlobalPaused(boolean paused) {
        System.out.println("[DEBUG] FarmController.setGlobalPaused: " + this.globalPaused + " -> " + paused);
        this.globalPaused = paused;
        long now = System.currentTimeMillis();
        for (PlantInstance plant : plantField.getPlants()) {
            plant.setGlobalPaused(paused, now);
        }
    }

    public boolean isGlobalPaused() {
        return globalPaused;
    }

    public void setStatsManager(WormStatsManager manager) {
        this.statsManager = manager;
    }

    public CompostSource getCompostSource() {
        return compostSource;
    }

    public List<DungBeetle> getDungBeetles() {
        return dungBeetles;
    }

    public void addDungBeetle(DungBeetle beetle) {
        dungBeetles.add(beetle);
    }

    public PlantField getPlantField() {
        return plantField;
    }

    public MarketMarker getMarketMarker() {
        return marketMarker;
    }

    /**
     * Tick для движения объектов и анимаций (ставится на паузу при маркете/пятнашках/меню).
     * Рост и ускорение — в growthTimer!
     */
    public void tick() {
        if (globalPaused) return;
        for (DungBeetle beetle : dungBeetles) {
            beetle.tick();
        }
    }

    // --- ОТДЕЛЬНЫЙ ТАЙМЕР ДЛЯ РОСТА И УСКОРЕНИЯ ---
    private void startGrowthTimer() {
        growthTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!globalPaused) {
                    for (PlantInstance plant : plantField.getPlants()) {
                        DungBeetle beetle = plant.getAssignedBeetle();
                        if (beetle != null) {
                            plant.boostGrowth(beetle.getGrowthMultiplier());
                        }
                    }
                }
            }
        }, 33, 33); // ~30 раз в секунду
    }

    // --- АВТОУРЖАЙ (тоже не зависит от paused карты!) ---
    private void startHarvestTimer() {
        harvestTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!globalPaused) {
                    autoHarvestPlants();
                }
            }
        }, 1000, 1000);
    }

    private void autoHarvestPlants() {
        if (statsManager == null) return;
        long now = System.currentTimeMillis();
        for (PlantInstance plant : plantField.getPlants()) {
            if (plant.isReadyToHarvest(now)) {
                statsManager.addCoins(1);
                System.out.println("[DEBUG] FarmController.autoHarvestPlants: Plant " + plant.getX() + "," + plant.getY() + " harvested! addCoins(1)");
                plant.resetGrowth(now);
                // assignedBeetle не сбрасываем!
            }
        }
    }

    // --- Сериализация и восстановление ---

    public FarmSaveData toSaveData() {
        List<PlantData> plantDatas = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (PlantInstance p : plantField.getPlants()) {
            plantDatas.add(new PlantData(
                    p.getX(),
                    p.getY(),
                    p.getPlantedAtMillis(),
                    p.getGrowthMillis(now),
                    p.isPaused()
            ));
        }
        List<DungBeetleData> beetleDatas = new ArrayList<>();
        for (DungBeetle b : dungBeetles) {
            Integer plantIndex = null;
            PlantInstance assigned = b.getTargetPlant();
            if (assigned != null) {
                plantIndex = plantField.getPlants().indexOf(assigned);
            }
            beetleDatas.add(new DungBeetleData(
                    b.getX(),
                    b.getY(),
                    b.getBeetleState().name(),
                    plantIndex,
                    b.isCarryingBall()
            ));
        }
        return new FarmSaveData(plantDatas, beetleDatas);
    }

    public void restoreFromSave(FarmSaveData saveData) {
        plantField.getPlants().clear();
        for (PlantData pd : saveData.getPlants()) {
            PlantInstance plant = new PlantInstance(pd.getX(), pd.getY(), pd.getPlantedAtMillis());
            plant.setGrowthAccumulatedMillis(pd.getGrowthAccumulatedMillis());
            plant.setPaused(pd.isPaused(), pd.getPlantedAtMillis());
            plantField.getPlants().add(plant);
            System.out.println("[DEBUG] restoreFromSave: Plant x=" + pd.getX() + " y=" + pd.getY() + " paused=" + pd.isPaused() + " plantedAtMillis=" + pd.getPlantedAtMillis() + " growthAccum=" + pd.getGrowthAccumulatedMillis());
        }
        dungBeetles.clear();
        List<PlantInstance> plants = plantField.getPlants();
        for (DungBeetleData bd : saveData.getBeetles()) {
            DungBeetle beetle = new DungBeetle(bd.getX(), bd.getY(), compostSource, 2, 4, 2.0);
            PlantInstance assignedPlant = null;
            if (bd.getAssignedPlantIndex() != null && bd.getAssignedPlantIndex() < plants.size()) {
                assignedPlant = plants.get(bd.getAssignedPlantIndex());
                beetle.assignToPlant(assignedPlant);
            }
            beetle.setBeetleState(DungBeetle.BeetleState.valueOf(bd.getBeetleState()));
            beetle.setHasBall(bd.hasBall());

            switch (beetle.getBeetleState()) {
                case TO_COMPOST, TO_COMPOST_NO_BALL -> beetle.setTarget(compostSource.getX(), compostSource.getY());
                case WITH_BALL_TO_PLANT -> {
                    if (assignedPlant != null)
                        beetle.setTarget(assignedPlant.getX(), assignedPlant.getY());
                }
                case IDLE -> {}
            }

            dungBeetles.add(beetle);
            System.out.println("[DEBUG] restoreFromSave: Beetle x=" + bd.getX() + " y=" + bd.getY() + " state=" + bd.getBeetleState() + " plantIdx=" + bd.getAssignedPlantIndex() + " hasBall=" + bd.hasBall());
        }
    }
}