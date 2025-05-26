package com.wormfarm.farm.ui;

import com.wormfarm.farm.resource.Beehive;

import java.awt.*;

public class BeehiveRenderer {
    public static void drawBeehive(Graphics2D g, Beehive beehive) {
        int x = beehive.getX();
        int y = beehive.getY();
        int r = beehive.getRadius();

        // Основная форма улья: жёлто-оранжевые слои (3 овала)
        g.setColor(new Color(250, 220, 80, 160));
        g.fillOval(x - r, y - r, r * 2, r * 2);

        g.setColor(new Color(240, 200, 40, 180));
        g.fillOval(x - (int)(r * 0.8), y - (int)(r * 0.6), (int)(r * 1.6), (int)(r * 1.4));

        g.setColor(new Color(210, 170, 30, 200));
        g.fillOval(x - (int)(r * 0.5), y, (int)(r * 1.0), (int)(r * 0.8));

        // Контур улья
        g.setColor(new Color(170, 120, 10, 180));
        g.drawOval(x - r, y - r, r * 2, r * 2);

        // Вход в улей (тёмно-коричневый кружок)
        g.setColor(new Color(90, 60, 20, 220));
        int holeR = r / 3;
        g.fillOval(x - holeR, y + r/3, holeR * 2, holeR * 2);
    }
}