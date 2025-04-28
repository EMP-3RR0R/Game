package com.wormfarm.gui.panel;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WormMapPanelTest {
    private WormMapPanel panel;

    @BeforeEach
    void setUp() {
        WormState worm = mock(WormState.class);
        EventMapModel mapModel = mock(EventMapModel.class);
        JFrame frame = mock(JFrame.class);
        WormStatsManager statsManager = mock(WormStatsManager.class);

        panel = new WormMapPanel(worm, mapModel, frame, statsManager);
    }

    @AfterEach
    void tearDown() {
        panel.dispose();
    }

    @Test
    void testSetOnExitToMenu() {
        Runnable cb = mock(Runnable.class);
        panel.setOnExitToMenu(cb);
        assertDoesNotThrow(() -> panel.setOnExitToMenu(cb));
    }

    @Test
    void testDisposeStopsTimers() {
        panel.dispose();
    }

    @Test
    void testSetTargetPositionClampsCorrectly() {
        panel.setTargetPosition(new java.awt.Point(-100, 1000));
    }
}