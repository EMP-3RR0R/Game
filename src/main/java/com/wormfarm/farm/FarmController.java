package com.wormfarm.farm;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.*;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.insect.Bee;
import com.wormfarm.farm.insect.Ant;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.farm.resource.Beehive;
import com.wormfarm.farm.resource.Anthill;
import com.wormfarm.farm.plant.PlantField;
import com.wormfarm.farm.market.MarketMarker;
import com.wormfarm.farm.plant.PlantInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FarmController {
    private final List<DungBeetle> dungBeetles = new ArrayList<>();
    private final List<Bee> bees = new ArrayList<>();
    private final List<Ant> ants = new ArrayList<>();
    private final CompostSource compostSource;
    private final Beehive beehive = new Beehive(100, 700, 30);
    private final Anthill anthill = new Anthill(400, 700, 30);
    private final PlantField plantField = new PlantField();
    private final MarketMarker marketMarker = new MarketMarker(320, 100);

    private WormStatsManager statsManager;

    private final Timer growthTimer = new Timer("PlantGrowthTimer", true);
    private final Timer harvestTimer = new Timer("AutoHarvestTimer", true);

    private volatile boolean globalPaused = false;

    public FarmController(CompostSource compostSource, WormStatsManager statsManager) {
        this.compostSource = compostSource;
        this.statsManager = statsManager;
        beehive.setFarmController(this);
        anthill.setFarmController(this);
        startGrowthTimer();
        startHarvestTimer();
    }

    public void shutdown() {
        try {
            growthTimer.cancel();
        } catch (Exception ignored) {}
        try {
            harvestTimer.cancel();
        } catch (Exception ignored) {}
    }

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

    public Beehive getBeehive() {
        return beehive;
    }

    public Anthill getAnthill() {
        return anthill;
    }

    public List<DungBeetle> getDungBeetles() {
        return dungBeetles;
    }

    public List<Bee> getBees() {
        return bees;
    }

    public List<Ant> getAnts() {
        return ants;
    }

    public void addDungBeetle(DungBeetle beetle) {
        dungBeetles.add(beetle);
    }

    public void addBee(Bee bee) {
        bees.add(bee);
    }

    public void addAnt(Ant ant) {
        ants.add(ant);
    }

    public PlantField getPlantField() {
        return plantField;
    }

    public MarketMarker getMarketMarker() {
        return marketMarker;
    }

    public void tick() {
        if (globalPaused) return;
        for (DungBeetle beetle : dungBeetles) {
            beetle.tick();
        }
        for (Bee bee : bees) {
            bee.tick();
        }
        for (Ant ant : ants) {
            ant.tick();
        }
    }

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
        }, 33, 33);
    }

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
                int price = (int)Math.round(1 * plant.getPriceMultiplier());
                statsManager.addCoins(price);
                System.out.println("[DEBUG] FarmController.autoHarvestPlants: Plant " + plant.getX() + "," + plant.getY() + " harvested! addCoins(" + price + ")");
                plant.resetGrowth(now);
            }
        }
    }

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

        List<BeeData> beeDatas = new ArrayList<>();
        for (Bee b : bees) {
            Integer plantIndex = null;
            PlantInstance assigned = b.getTargetPlant();
            if (assigned != null) {
                plantIndex = plantField.getPlants().indexOf(assigned);
            }
            beeDatas.add(new BeeData(
                    b.getX(),
                    b.getY(),
                    b.getBeeState().name(),
                    plantIndex,
                    b.isCarryingNectar(),
                    b.getEllipseProgress(),
                    b.getVisualDirectionRad()
            ));
        }

        List<AntData> antDatas = new ArrayList<>();
        for (Ant a : ants) {
            Integer plantIndex = null;
            PlantInstance assigned = a.getTargetPlant();
            if (assigned != null) {
                plantIndex = plantField.getPlants().indexOf(assigned);
            }
            antDatas.add(new AntData(
                    a.getX(),
                    a.getY(),
                    a.getAntState().name(),
                    plantIndex,
                    a.isCarryingAphid(),
                    a.getEllipseProgress(),
                    a.getVisualDirectionRad()
            ));
        }

        return new FarmSaveData(plantDatas, beetleDatas, beeDatas, antDatas);
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
                case IDLE -> {
                }
            }

            dungBeetles.add(beetle);
            System.out.println("[DEBUG] restoreFromSave: Beetle x=" + bd.getX() + " y=" + bd.getY() + " state=" + bd.getBeetleState() + " plantIdx=" + bd.getAssignedPlantIndex() + " hasBall=" + bd.hasBall());
        }
        bees.clear();
        for (BeeData bd : saveData.getBees()) {

            Bee bee = new Bee(bd.getX(), bd.getY(), beehive, 3);
            Bee.BeeState state = Bee.BeeState.valueOf(bd.getBeeState());
            bee.setBeeState(state);
            bee.setHasNectar(bd.hasNectar());
            bee.setEllipseProgress(bd.getEllipseProgress());
            bee.setVisualDirectionRad(bd.getVisualDirectionRad());

            PlantInstance assignedPlant = null;
            if (bd.getAssignedPlantIndex() != null && bd.getAssignedPlantIndex() < plants.size()) {
                assignedPlant = plants.get(bd.getAssignedPlantIndex());

                bee.setTargetPlant(assignedPlant);

                switch (state) {
                    case TO_PLANT:
                        bee.setTarget(assignedPlant.getX(), assignedPlant.getY());
                        break;
                    case WITH_NECTAR_TO_HIVE:
                        bee.setTarget(beehive.getX(), beehive.getY());
                        break;
                    case IDLE:
                        break;
                }
            } else {
            }

            bee.setX(bd.getX());
            bee.setY(bd.getY());

            bees.add(bee);
        }
        ants.clear();
        for (AntData ad : saveData.getAnts()) {
            Ant ant = new Ant(ad.getX(), ad.getY(), anthill, 2);

            ant.setAntState(Ant.AntState.valueOf(ad.getAntState()));
            ant.setHasAphid(ad.hasAphid());
            ant.setEllipseProgress(ad.getEllipseProgress()); // Ключевая строка!
            ant.setVisualDirectionRad(ad.getVisualDirectionRad());

            if (ad.getAssignedPlantIndex() != null) {
                PlantInstance plant = plants.get(ad.getAssignedPlantIndex());
                ant.setTargetPlant(plant);

                if (ant.getAntState() == Ant.AntState.TO_PLANT) {
                    ant.setTarget(plant.getX(), plant.getY());
                } else if (ant.getAntState() == Ant.AntState.WITH_APHID_TO_ANTHILL) {
                    ant.setTarget(anthill.getX(), anthill.getY());
                }
            }

            ants.add(ant);
        }
    }
}