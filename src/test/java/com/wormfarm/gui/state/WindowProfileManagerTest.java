package com.wormfarm.gui.state;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WindowProfileManagerTest {

    private JFrame frame;
    private JDesktopPane desktopPane;
    private GameSessionManager gameSessionManager;
    private WindowProfileManager profileManager;

    @BeforeEach
    void setup() {
        frame = new JFrame();
        desktopPane = new JDesktopPane();

        gameSessionManager = mock(GameSessionManager.class);
        profileManager = new WindowProfileManager(frame, desktopPane, gameSessionManager);
    }

    @AfterEach
    void cleanup() {
        frame.dispose();
        try {
            Files.walk(Paths.get("window_profiles"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {}
    }

    @Test
    void testOnlyMaximized() {
        frame.setBounds(200, 200, 500, 400);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setExtendedState(JFrame.NORMAL);
        profileManager.restoreWindowsProfile(1);

        assertEquals(JFrame.MAXIMIZED_BOTH, frame.getExtendedState());
    }

    @Test
    void testOnlyMinimized() {
        frame.setBounds(300, 300, 400, 300);
        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setExtendedState(JFrame.NORMAL);
        profileManager.restoreWindowsProfile(1);

        assertEquals(JFrame.ICONIFIED, frame.getExtendedState());
    }

    @Test
    void testOnlyMovedAndResized() {
        frame.setBounds(100, 100, 640, 480);
        frame.setExtendedState(JFrame.NORMAL);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setBounds(400, 400, 300, 200);
        profileManager.restoreWindowsProfile(1);

        Rectangle bounds = frame.getBounds();
        assertEquals(100, bounds.x);
        assertEquals(100, bounds.y);
        assertEquals(640, bounds.width);
        assertEquals(480, bounds.height);
    }

    @Test
    void testMovedAndMinimized() {
        frame.setBounds(250, 250, 900, 600);
        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setBounds(0, 0, 200, 200);
        frame.setExtendedState(JFrame.NORMAL);

        profileManager.restoreWindowsProfile(1);

        Rectangle bounds = frame.getBounds();
        assertEquals(250, bounds.x);
        assertEquals(250, bounds.y);
        assertEquals(900, bounds.width);
        assertEquals(600, bounds.height);
        assertEquals(JFrame.ICONIFIED, frame.getExtendedState());
    }

    @Test
    void testMaximizedAndMinimized() {
        frame.setBounds(50, 50, 500, 500);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH | JFrame.ICONIFIED);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setExtendedState(JFrame.NORMAL);
        profileManager.restoreWindowsProfile(1);

        assertFalse((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
        assertTrue((frame.getExtendedState() & JFrame.ICONIFIED) != 0);
    }

    @Test
    void testMovedMaximizedAndMinimized() {
        frame.setBounds(350, 350, 1280, 720);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH | JFrame.ICONIFIED);
        frame.setVisible(true);
        profileManager.saveWindowsProfile();

        frame.setExtendedState(JFrame.NORMAL);
        frame.setBounds(0, 0, 100, 100);

        profileManager.restoreWindowsProfile(1);

        Rectangle bounds = frame.getBounds();
        assertEquals(350, bounds.x);
        assertEquals(350, bounds.y);
        assertEquals(1280, bounds.width);
        assertEquals(720, bounds.height);
        assertFalse((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
        assertTrue((frame.getExtendedState() & JFrame.ICONIFIED) != 0);
    }
}
