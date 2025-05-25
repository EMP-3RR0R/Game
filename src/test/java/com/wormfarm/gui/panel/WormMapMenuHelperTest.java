package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.settings.UserSettings;
import org.junit.jupiter.api.*;
import javax.swing.*;

import java.awt.*;

import static org.mockito.Mockito.*;

class WormMapMenuHelperTest {

    @AfterEach
    void tearDown() {

        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) {
                w.dispose();
            }
        }
    }

    @Test
    void testShowPauseMenu_WithDesktopPane() {
        WormMapPanel panel = mock(WormMapPanel.class);
        WormState worm = mock(WormState.class);
        WormStatsManager stats = mock(WormStatsManager.class);
        UserSettings settings = mock(UserSettings.class);

        JDesktopPane desktopPane = new JDesktopPane();
        when(panel.getDesktopPane()).thenReturn(desktopPane);
        when(panel.getTargetX()).thenReturn(100);
        when(panel.getTargetY()).thenReturn(200);
        when(panel.getGameSessionManager()).thenReturn(mock(GameSessionManager.class));
    }

    @Test
    void testShowPauseMenu_WithoutDesktopPane() {
        WormMapPanel panel = mock(WormMapPanel.class);
        WormState worm = mock(WormState.class);
        WormStatsManager stats = mock(WormStatsManager.class);

        when(panel.getDesktopPane()).thenReturn(null);

        WormMapMenuHelper helper = new WormMapMenuHelper(panel, worm, stats);

        try (var mocked = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mocked.when(() -> JOptionPane.showMessageDialog(any(), any())).then(inv -> null);
            helper.showPauseMenu(
                    mock(JFrame.class),
                    mock(Runnable.class),
                    mock(Runnable.class),
                    mock(Runnable.class),
                    mock(Runnable.class)
            );
        }
    }
}