package com.wormfarm.farm.ui;

import com.wormfarm.farm.insect.DungBeetle;

import java.awt.*;

public class DungBeetleRenderer {
    public static void drawDungBeetle(Graphics2D g, DungBeetle beetle) {
        int x = beetle.getX();
        int y = beetle.getY();

        // Если катит шар навоза — рисуем большой круг впереди
        if (beetle.shouldDrawBall()) {
            int[] ballPos = beetle.getBallPosition();
            int bx = (ballPos != null) ? ballPos[0] : x;
            int by = (ballPos != null) ? ballPos[1] : y;
            g.setColor(new Color(130, 90, 35, 230));
            g.fillOval(bx - 14, by - 14, 28, 28); // Большой шар
            g.setColor(new Color(80, 50, 20, 240));
            g.drawOval(bx - 14, by - 14, 28, 28);
        }

        // Сам жук — средний круг
        g.setColor(new Color(45, 40, 30));
        g.fillOval(x - 9, y - 9, 18, 18);
        g.setColor(Color.BLACK);
        g.drawOval(x - 9, y - 9, 18, 18);

        // Можно добавить лапки, глазки и т.д.
    }
}