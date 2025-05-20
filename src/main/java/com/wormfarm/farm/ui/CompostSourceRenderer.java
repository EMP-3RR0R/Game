package com.wormfarm.farm.ui;

import com.wormfarm.farm.resource.CompostSource;

import java.awt.*;

public class CompostSourceRenderer {
    public static void drawCompostSource(Graphics2D g, CompostSource compost) {
        int x = compost.getX();
        int y = compost.getY();
        int r = compost.getRadius();

        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x - r, y - r, r * 2, r * 2);
        g.setColor(new Color(30, 30, 30, 180));
        g.drawOval(x - r, y - r, r * 2, r * 2);
    }
}