package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.logic.WormStatsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ResourceBundle;

public class WormMapRenderer {
    private WormMapRenderer() {}

    public static void paintWholeMap(
            JPanel panel, Graphics g, WormState worm, EventMapModel mapModel,
            int targetX, int targetY, WormStatsManager statsManager,
            ResourceBundle messages
    ) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Фон
        WormMapResources.paintGround(g2d, panel.getWidth(), panel.getHeight());

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(0, 0, panel.getWidth(), panel.getHeight());

        drawWorm(g2d, (int)worm.getX(), (int)worm.getY(), worm.getDirection());
        drawTarget(g2d, targetX, targetY);

        for (EventMarker marker : mapModel.getMarkers()) {
            if ("puzzle.title".equals(marker.getDescription())) {
                drawFifteenPuzzleIcon(g2d, marker);
            } else {
                drawMarker(g2d, marker);
            }
        }

        drawWormCoinCounter(g2d, statsManager, panel.getWidth());
        g2d.dispose();
    }

    private static void drawWormCoinCounter(Graphics2D g2d, WormStatsManager statsManager, int fieldWidth) {
        if (statsManager == null) return;
        final int padding = 12;
        final int iconSize = WormMapResources.WORMCOIN_ICON_SIZE;
        int coins = statsManager.getCoins();

        Font font = new Font("Arial", Font.BOLD, 22);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        String coinText = String.valueOf(coins);
        int textWidth = fm.stringWidth(coinText);
        int totalWidth = iconSize + 8 + textWidth + 2*padding;
        int height = Math.max(iconSize, fm.getHeight()) + padding;
        int x = fieldWidth - totalWidth;
        int y = padding;

        g2d.setColor(new Color(255,255,255,200));
        g2d.fillRoundRect(x, y, totalWidth, height, 18, 18);

        g2d.setColor(new Color(200,200,200, 225));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, totalWidth, height, 18, 18);

        if (WormMapResources.wormCoinIcon != null) {
            g2d.drawImage(WormMapResources.wormCoinIcon, x + padding, y + (height-iconSize)/2, iconSize, iconSize, null);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(x + padding, y + (height-iconSize)/2, iconSize, iconSize);
        }

        g2d.setColor(new Color(30, 30, 30));
        int textY = y + (height + fm.getAscent() - fm.getDescent())/2 - 2;
        g2d.drawString(coinText, x + padding + iconSize + 8, textY);
    }

    static void drawFifteenPuzzleIcon(Graphics2D g, EventMarker marker) {
        int zoneRadius = 40;
        g.setColor(new Color(100, 200, 255, 60));
        int zoneX = marker.getX() - zoneRadius;
        int zoneY = marker.getY() - zoneRadius;
        g.fillOval(zoneX, zoneY, zoneRadius * 2, zoneRadius * 2);

        if (WormMapResources.fifteenPuzzleIcon == null) {
            drawMarker(g, marker);
            return;
        }
        int size = 72;
        int x = marker.getX() - size / 2 + 2;
        int y = marker.getY() - size / 2 + 5;
        g.drawImage(WormMapResources.fifteenPuzzleIcon, x, y, size, size, null);
    }

    static void drawWorm(Graphics2D g, int x, int y, double direction) {
        AffineTransform oldTransform = g.getTransform();
        g.setTransform(AffineTransform.getRotateInstance(direction, x, y));
        g.setColor(Color.MAGENTA);
        g.fillOval(x - 15, y - 5, 30, 10);
        g.setColor(Color.BLACK);
        g.drawOval(x - 15, y - 5, 30, 10);
        g.setTransform(oldTransform);
    }

    static void drawTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.GREEN);
        g.fillOval(x - 5, y - 5, 10, 10);
    }

    static void drawMarker(Graphics2D g, EventMarker marker) {
        g.setColor(Color.RED);
        g.fillRect(marker.getX() - 10, marker.getY() - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(marker.getX() - 10, marker.getY() - 10, 20, 20);
    }
}