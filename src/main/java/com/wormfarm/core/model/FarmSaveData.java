package com.wormfarm.core.model;

import java.io.Serializable;
import java.util.List;

public class FarmSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<PlantData> plants;
    private final List<DungBeetleData> beetles;
    private final List<BeeData> bees;
    private final List<AntData> ants;

    public FarmSaveData(List<PlantData> plants, List<DungBeetleData> beetles, List<BeeData> bees, List<AntData> ants) {
        this.plants = plants;
        this.beetles = beetles;
        this.bees = bees;
        this.ants = ants;
    }

    public List<PlantData> getPlants() { return plants; }
    public List<DungBeetleData> getBeetles() { return beetles; }
    public List<BeeData> getBees() { return bees; }
    public List<AntData> getAnts() { return ants; }
}