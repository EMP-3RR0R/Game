package com.wormfarm.gui.panel;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class WormMapResourcesTest {
    @Test
    void testResizeTexture_ResizesCorrectly() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        BufferedImage result = WormMapResources.resizeTexture(img, 32, 24);
        assertEquals(32, result.getWidth());
        assertEquals(24, result.getHeight());
    }

    @Test
    void testPaintGround_NoGroundTexture() {
        WormMapResources.groundTexture = null;
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        assertDoesNotThrow(() -> WormMapResources.paintGround(g2d, 50, 50));
        g2d.dispose();
    }

    @Test
    void testPaintGround_WithGroundTexture() {
        WormMapResources.groundTexture = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        assertDoesNotThrow(() -> WormMapResources.paintGround(g2d, 50, 50));
        g2d.dispose();
    }
}