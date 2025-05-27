package com.wormfarm.farm.ui;

import com.wormfarm.farm.resource.Anthill;

import java.awt.*;

public class AnthillRenderer {
    public static void drawAnthill(Graphics2D g, Anthill anthill) {
        int x = anthill.getX();
        int y = anthill.getY();
        int r = anthill.getRadius();

        g.setColor(new Color(120, 70, 30, 120));
        g.fillOval(x - r, y - r, r * 2, r * 2);

        g.setColor(new Color(80, 45, 15, 180));
        g.drawOval(x - r, y - r, r * 2, r * 2);

        g.setColor(new Color(30, 20, 10, 200));
        g.fillOval(x - r/2, y + r/3, r, r/2);
    }
}