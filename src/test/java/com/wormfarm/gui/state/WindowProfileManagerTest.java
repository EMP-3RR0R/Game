package com.wormfarm.gui.state;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;

class WindowProfileManagerTest {
    JFrame frame;
    ResourceBundle messages;
    JDesktopPane desktopPane;
    GameSessionManager gsm;
    WindowProfileManager wpm;

    @BeforeEach
    void setUp() {
        frame = mock(JFrame.class);
        messages = mock(ResourceBundle.class);
        desktopPane = mock(JDesktopPane.class);
        gsm = mock(GameSessionManager.class);

        when(messages.getString(anyString())).thenAnswer(inv -> inv.getArguments()[0] + "_msg");

        wpm = new WindowProfileManager(frame, messages, desktopPane, gsm);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) w.dispose();
        }
        try {
            if (Files.exists(Paths.get("window_profiles"))) {
                Files.walk(Paths.get("window_profiles"))
                        .sorted((a, b) -> b.toString().length() - a.toString().length())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (Exception ignored) {}
    }

    @Test
    void testSaveWindowsProfile_DoesNotCrash() {
        wpm.saveWindowsProfile();
    }

    @Test
    void testRestoreWindowsProfile_NoFile() {
        try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mockJOP.when(() -> JOptionPane.showMessageDialog(any(), any())).then(inv -> null);
            wpm.restoreWindowsProfile(1);
        }
    }

    @Test
    void testCreateMenuBarProfiles_NotNull() {
        JMenuBar bar = wpm.createMenuBarProfiles();
        Assertions.assertNotNull(bar);
        Assertions.assertTrue(bar.getMenuCount() > 0);
    }
}