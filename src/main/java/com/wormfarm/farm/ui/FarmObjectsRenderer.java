package com.wormfarm.farm.ui;

import com.wormfarm.farm.FarmController;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.market.MarketMarker;

import java.awt.*;

public class FarmObjectsRenderer {
    public static void drawFarmObjects(Graphics2D g, FarmController farm) {
        // Компостный источник
        if (farm.getCompostSource() != null) {
            CompostSourceRenderer.drawCompostSource(g, farm.getCompostSource());
        }
        // Растения
        PlantFieldRenderer.drawPlants(g, farm.getPlantField());
        // Жуки-навозники
        for (DungBeetle beetle : farm.getDungBeetles()) {
            DungBeetleRenderer.drawDungBeetle(g, beetle);
        }
        // Магазин
        MarketMarker market = farm.getMarketMarker();
        if (market != null) {
            drawMarketMarker(g, market);
        }
    }

    public static void drawMarketMarker(Graphics2D g, MarketMarker marker) {
        int x = marker.getX();
        int y = marker.getY();
        int size = marker.getSize();

        g.setColor(new Color(255, 215, 0));
        g.fillRect(x - size/2, y - size/2, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(x - size/2, y - size/2, size, size);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String s = "₩";
        int sw = fm.stringWidth(s);
        g.setColor(Color.DARK_GRAY);
        g.drawString(s, x - sw/2, y + fm.getAscent()/2 - 2);
    }
}