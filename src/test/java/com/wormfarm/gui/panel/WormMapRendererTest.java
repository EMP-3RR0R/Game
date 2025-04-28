package com.wormfarm.gui.panel;

import com.wormfarm.core.model.EventMarker;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.awt.image.BufferedImage;

class WormMapRendererTest {
    private Graphics2D g2d;

    @BeforeEach
    void setUp() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
    }

    @AfterEach
    void tearDown() {
        g2d.dispose();
    }

    @Test
    void testDrawMarker() {
        EventMarker marker = new EventMarker(10, 10, "Маркер");
        WormMapRenderer.drawMarker(g2d, marker);
    }

    @Test
    void testDrawTarget() {
        WormMapRenderer.drawTarget(g2d, 10, 10);
    }

    @Test
    void testDrawWorm() {
        WormMapRenderer.drawWorm(g2d, 10, 10, 0.0);
    }
}