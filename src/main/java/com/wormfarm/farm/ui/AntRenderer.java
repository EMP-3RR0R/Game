package com.wormfarm.farm.ui;

import com.wormfarm.farm.insect.Ant;

import java.awt.*;

public class AntRenderer {
    public static void drawAnt(Graphics2D g, Ant ant) {
        int x = ant.getX();
        int y = ant.getY();

        double angle = ant.getVisualDirectionRad();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(angle);

        g2.setColor(new Color(110, 70, 30, 255));
        g2.fillOval(-17, -8, 20, 16);

        g2.setColor(new Color(90, 60, 25, 255));
        g2.fillOval(0, -7, 15, 14);

        g2.setColor(new Color(80, 45, 18, 255));
        g2.fillOval(12, -5, 9, 9);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(60, 30, 10, 255));
        g2.drawLine(16, -5, 22, -13);
        g2.drawLine(16, 5, 22, 13);

        if (ant.isCarryingAphid()) {
            g2.setColor(new Color(130, 200, 90, 255));
            g2.fillOval(-15, -4, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawOval(-15, -4, 10, 10);
        }

        g2.dispose();
    }
}