package com.wormfarm.farm.ui;

import com.wormfarm.farm.plant.PlantField;
import com.wormfarm.farm.plant.PlantInstance;

import java.awt.*;

public class PlantFieldRenderer {
    public static void drawPlants(Graphics2D g, PlantField field) {
        for (PlantInstance plant : field.getPlants()) {
            g.setColor(new Color(0, 180, 60));
            g.fillOval(plant.getX() - 5, plant.getY() - 5, 10, 10);
            g.setColor(new Color(0, 100, 0));
            g.drawOval(plant.getX() - 5, plant.getY() - 5, 10, 10);
        }
    }
}