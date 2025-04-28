package com.wormfarm.gui.panel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class WormMapResources {
    public static BufferedImage groundTexture = null;
    public static BufferedImage fifteenPuzzleIcon = null;
    public static BufferedImage wormCoinIcon = null;

    public static final int GROUND_TEXTURE_SIZE = 64;
    public static final int WORMCOIN_ICON_SIZE = 32;

    static {
        // Земля
        try (InputStream in = WormMapResources.class.getResourceAsStream("/images/ground.png")) {
            if (in != null) {
                BufferedImage orig = ImageIO.read(in);
                if (orig != null) {
                    groundTexture = resizeTexture(orig, GROUND_TEXTURE_SIZE, GROUND_TEXTURE_SIZE);
                }
            }
        } catch (Exception e) {
            groundTexture = null;
        }
        // Иконка пятнашек
        try (InputStream in = WormMapResources.class.getResourceAsStream("/images/FifteenPuzzle/FifteenPuzzleIcon.png")) {
            if (in != null) {
                fifteenPuzzleIcon = ImageIO.read(in);
            }
        } catch (Exception e) {
            fifteenPuzzleIcon = null;
        }
        // Иконка WormCoin
        try (InputStream in = WormMapResources.class.getResourceAsStream("/images/WormCoinIcon.png")) {
            if (in != null) {
                BufferedImage orig = ImageIO.read(in);
                if (orig != null) {
                    wormCoinIcon = resizeTexture(orig, WORMCOIN_ICON_SIZE, WORMCOIN_ICON_SIZE);
                }
            }
        } catch (Exception e) {
            wormCoinIcon = null;
        }
    }

    public static BufferedImage resizeTexture(BufferedImage img, int targetW, int targetH) {
        Image scaled = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        BufferedImage small = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = small.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return small;
    }

    public static void paintGround(Graphics2D g2d, int width, int height) {
        if (groundTexture != null) {
            int texW = groundTexture.getWidth();
            int texH = groundTexture.getHeight();
            for (int y = 0; y < height; y += texH) {
                for (int x = 0; x < width; x += texW) {
                    g2d.drawImage(groundTexture, x, y, null);
                }
            }
        } else {
            g2d.setColor(new Color(120, 120, 120));
            g2d.fillRect(0, 0, width, height);
        }
    }
}