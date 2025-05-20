package com.wormfarm.core.model;

import java.io.Serializable;
import java.util.List;

public class FarmSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<PlantData> plants;
    private final List<DungBeetleData> beetles;

    public FarmSaveData(List<PlantData> plants, List<DungBeetleData> beetles) {
        this.plants = plants;
        this.beetles = beetles;
    }

    public List<PlantData> getPlants() { return plants; }
    public List<DungBeetleData> getBeetles() { return beetles; }
}