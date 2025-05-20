package com.wormfarm.farm.plant;

import java.util.ArrayList;
import java.util.List;

public class PlantField {
    private final List<PlantInstance> plants = new ArrayList<>();
    public static final int PLANT_START_X = 300;
    public static final int PLANT_START_Y = 300;
    public static final int PLANT_INTERVAL = 20;
    public static final int FIELD_COLS = 15; // 15 по ширине
    public static final int FIELD_ROWS = 15; // 15 по высоте
    public static final int MAX_PLANTS = 225; // 15*15

    public PlantInstance tryAddPlant(long now) {
        if (plants.size() >= MAX_PLANTS) return null;
        int index = plants.size();
        int col = index % FIELD_COLS;
        int row = index / FIELD_COLS;
        int x = PLANT_START_X + col * PLANT_INTERVAL;
        int y = PLANT_START_Y + row * PLANT_INTERVAL;
        PlantInstance plant = new PlantInstance(x, y, now);
        plants.add(plant);
        return plant;
    }

    public boolean isFull() {
        return plants.size() >= MAX_PLANTS;
    }

    public List<PlantInstance> getPlants() {
        return plants;
    }
}