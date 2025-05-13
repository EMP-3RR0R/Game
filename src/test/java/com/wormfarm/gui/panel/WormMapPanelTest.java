package com.wormfarm.gui.panel;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.gui.state.GameSessionManager;
import org.junit.jupiter.api.*;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.ResourceBundle;

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

        panel = new WormMapPanel(worm, mapModel, frame, statsManager, null);
    }

    @AfterEach
    void tearDown() {
        panel.shutdown();
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) {
                w.dispose();
            }
        }
    }

    @Test
    void testSetOnExitToMenu() {
        Runnable cb = mock(Runnable.class);
        assertDoesNotThrow(() -> panel.setOnExitToMenu(cb));
    }

    @Test
    void testSetOnExitToDesktop() {
        Runnable cb = mock(Runnable.class);
        assertDoesNotThrow(() -> panel.setOnExitToDesktop(cb));
    }

    @Test
    void testSetOnLanguageChanged() {
        Runnable cb = mock(Runnable.class);
        assertDoesNotThrow(() -> panel.setOnLanguageChanged(cb));
    }

    @Test
    void testSetGameSessionManagerAndGet() {
        GameSessionManager gsm = mock(GameSessionManager.class);
        panel.setGameSessionManager(gsm);
        assertSame(gsm, panel.getGameSessionManager());
    }

    @Test
    void testSetAndGetDesktopPane() {
        JDesktopPane dp = new JDesktopPane();
        panel.setDesktopPane(dp);
        assertSame(dp, panel.getDesktopPane());
    }

    @Test
    void testSetTargetPositionClampsCorrectly() {
        panel.setTargetPosition(new Point(-100, 1000));
        assertTrue(panel.getTargetX() >= 0 && panel.getTargetX() <= WormMapPanel.FIELD_WIDTH);
        assertTrue(panel.getTargetY() >= 0 && panel.getTargetY() <= WormMapPanel.FIELD_HEIGHT);
    }

    @Test
    void testSetTarget() {
        panel.setTarget(-10, 9000);
        assertTrue(panel.getTargetX() >= 0 && panel.getTargetX() <= WormMapPanel.FIELD_WIDTH);
        assertTrue(panel.getTargetY() >= 0 && panel.getTargetY() <= WormMapPanel.FIELD_HEIGHT);
    }

    @Test
    void testPauseGameResumeGame() {
        panel.pauseGame();
        assertTrue(panel.isPaused());
        panel.resumeGame();
        assertFalse(panel.isPaused());
    }

    @Test
    void testUpdateLocaleNotCrashing() {
        assertDoesNotThrow(() -> panel.updateLocale());
    }

    @Test
    void testTryActivateEvent_NoException() {
        var marker = new com.wormfarm.core.model.EventMarker(10, 20, "puzzle.title");
        ResourceBundle bundle = new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return key;
            }
            @Override
            public Enumeration<String> getKeys() {
                return java.util.Collections.enumeration(java.util.Arrays.asList(
                        "puzzle.title", "challenge.confirm.message", "challenge.confirm.title", "desc"
                ));
            }
        };
        try {
            var field = WormMapPanel.class.getDeclaredField("messages");
            field.setAccessible(true);
            field.set(panel, bundle);

            try (var mocked = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
                mocked.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt()))
                        .thenReturn(JOptionPane.YES_OPTION);
                assertDoesNotThrow(() -> panel.tryActivateEvent(marker));
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testPaintComponentNotCrashing() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.createGraphics();
        assertDoesNotThrow(() -> panel.paintComponent(g));
        g.dispose();
    }

    @Test
    void testShutdownRemovesFromParent() {
        JPanel parent = new JPanel();
        parent.add(panel);
        assertSame(panel.getParent(), parent);
        panel.shutdown();
        assertNull(panel.getParent());
    }
}