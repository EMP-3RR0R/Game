package com.wormfarm.gui.panel;

import com.wormfarm.core.model.*;
import com.wormfarm.core.logic.WormStatsManager;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.util.*;

import static org.mockito.Mockito.*;

class WormMapEventManagerTest {
    private WormMapPanel panel;
    private EventMapModel mapModel;
    private WormMapEventManager mgr;

    @BeforeEach
    void setUp() {
        WormState worm = mock(WormState.class);
        mapModel = mock(EventMapModel.class);
        Set<EventMarker> activeMarkers = new HashSet<>();
        Map<EventMarker, Long> recentlyActivated = new HashMap<>();
        panel = mock(WormMapPanel.class);
        mgr = new WormMapEventManager(panel, worm, mapModel, activeMarkers, recentlyActivated, null);
    }

    @Test
    void testUpdateWormAndEvents_NoMarkers() {
        when(mapModel.getMarkers()).thenReturn(Collections.emptyList());
        mgr.updateWormAndEvents(0, 0, false);
    }

    @Test
    void testUpdateWormAndEvents_Paused() {
        when(mapModel.getMarkers()).thenReturn(Collections.emptyList());
        mgr.updateWormAndEvents(0, 0, true);
    }

    @Test
    void testActivateEvent_Puzzle() {
        WormStatsManager statsManager = mock(WormStatsManager.class);

        EventMarker marker = new EventMarker(100,100,"puzzle.title");
        JFrame owner = mock(JFrame.class);
        Runnable onClose = mock(Runnable.class);

        JDesktopPane desktopPane = mock(JDesktopPane.class);
        when(panel.getDesktopPane()).thenReturn(desktopPane);
        doNothing().when(panel).setPaused(anyBoolean());

        mgr.activateEvent(marker, owner, statsManager, onClose);
    }
}