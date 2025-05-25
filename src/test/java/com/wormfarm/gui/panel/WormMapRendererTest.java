package com.wormfarm.gui.panel;

import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.farm.FarmController;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;

class WormMapRendererTest {
    private Graphics2D g2d;

    @BeforeEach
    void setUp() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
    }

    @AfterEach
    void tearDown() {
        g2d.dispose();
    }

    @Test
    void testDrawMarker() {
        EventMarker marker = new EventMarker(30, 30, "desc");
        WormMapRenderer.drawMarker(g2d, marker);
    }

    @Test
    void testDrawTarget() {
        WormMapRenderer.drawTarget(g2d, 40, 40);
    }

    @Test
    void testDrawWorm() {
        WormMapRenderer.drawWorm(g2d, 50, 60, Math.PI / 4);
    }

    @Test
    void testDrawFifteenPuzzleIcon_NullIcon() {
        WormMapResources.fifteenPuzzleIcon = null;
        EventMarker marker = new EventMarker(100, 100, "puzzle.title");
        WormMapRenderer.drawFifteenPuzzleIcon(g2d, marker);
    }

    @Test
    void testDrawFifteenPuzzleIcon_WithIcon() {
        WormMapResources.fifteenPuzzleIcon = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        EventMarker marker = new EventMarker(100, 100, "puzzle.title");
        WormMapRenderer.drawFifteenPuzzleIcon(g2d, marker);
    }

    @Test
    void testPaintWholeMap_Comprehensive() {
        JPanel panel = new JPanel();
        WormState worm = mock(WormState.class);
        EventMapModel mapModel = mock(EventMapModel.class);
        WormStatsManager statsManager = mock(WormStatsManager.class);
        FarmController farmController = mock(FarmController.class);

        // Инициализируем все ресурсы, которые используются в рендере
        WormMapResources.fifteenPuzzleIcon = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        WormMapResources.wormCoinIcon = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);

        when(worm.getX()).thenReturn(70.0);
        when(worm.getY()).thenReturn(80.0);
        when(worm.getDirection()).thenReturn(Math.PI);
        when(mapModel.getMarkers()).thenReturn(java.util.Arrays.asList(
                new EventMarker(50, 60, "any"),
                new EventMarker(120, 150, "puzzle.title")
        ));
        when(statsManager.getCoins()).thenReturn(42);
    }
}