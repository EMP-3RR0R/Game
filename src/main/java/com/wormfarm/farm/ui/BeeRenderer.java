package com.wormfarm.farm.ui;

import com.wormfarm.farm.insect.Bee;

import java.awt.*;

public class BeeRenderer {
    public static void drawBee(Graphics2D g, Bee bee) {
        int x = bee.getX();
        int y = bee.getY();

        // Направление
        double angle = bee.getVisualDirectionRad();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(angle);

        // Если несёт нектар — рисуем жёлтый круг под пчелой
        if (bee.isCarryingNectar()) {
            g2.setColor(new Color(255, 220, 0, 70));
            g2.fillOval(-16, -16, 32, 32);
        }

        // Основное тело — полосатый овал
        for (int i = 0; i < 4; i++) {
            g2.setColor(i % 2 == 0 ? new Color(240, 200, 40) : Color.BLACK);
            g2.fillOval(-18 + i * 9, -7, 18, 14);
        }
        g2.setColor(Color.BLACK);
        g2.drawOval(-18, -7, 36, 14);

        // Головка (чёрная)
        g2.setColor(Color.BLACK);
        g2.fillOval(14, -7, 12, 14);

        // Усики
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(18, -5, 22, -13);
        g2.drawLine(18, 5, 22, 13);

        g2.dispose();
    }
}