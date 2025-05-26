package com.wormfarm.gui.state;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

import static org.mockito.Mockito.*;

class AppStateRestorerTest {
    JFrame frame;
    ResourceBundle messages;
    GameSessionManager gsm;
    WindowProfileManager wpm;
    DialogManager dm;

    @BeforeEach
    void setUp() {
        frame = mock(JFrame.class);
        messages = mock(ResourceBundle.class);
        gsm = mock(GameSessionManager.class);
        wpm = mock(WindowProfileManager.class);
        dm = mock(DialogManager.class);
        when(messages.getString(anyString())).thenAnswer(inv -> inv.getArguments()[0] + "_msg");
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) w.dispose();
        }
        try {
            Files.walk(Paths.get("window_profiles"))
                    .map(Path::toFile)
                    .sorted((a, b) -> b.getAbsolutePath().length() - a.getAbsolutePath().length())
                    .forEach(File::delete);
            Files.deleteIfExists(Paths.get("testdialogs.state.bin"));
        } catch (Exception ignored) {}
    }

    @Test
    void testTryRestoreAppState_NoProfiles_NoDialogs() {
        AppStateRestorer restorer = new AppStateRestorer(frame, messages, gsm, wpm, dm);
        restorer.tryRestoreAppState();
        verify(wpm, never()).restoreWindowsProfile(anyInt());
        verify(dm, never()).restoreDialogStates();
    }

    @Test
    void testTryRestoreAppState_ProfileExists_UserPicksFirst() throws Exception {
        Path dir = Paths.get("window_profiles");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("testprofile1.state.bin"));
        Files.createFile(dir.resolve("testprofile2.state.bin"));
        new File("testdialogs.state.bin").createNewFile();

        AppStateRestorer restorer = new AppStateRestorer(frame, messages, gsm, wpm, dm);

        try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mockJOP.when(() -> JOptionPane.showOptionDialog(
                            any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(1);

            restorer.tryRestoreAppState();
        }
    }

    @Test
    void testTryRestoreAppState_ProfileExists_UserDeclines() throws Exception {
        Path dir = Paths.get("window_profiles");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("testprofile1.state.bin"));

        AppStateRestorer restorer = new AppStateRestorer(frame, messages, gsm, wpm, dm);

        try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mockJOP.when(() -> JOptionPane.showOptionDialog(
                            any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(1);

            restorer.tryRestoreAppState();

            verify(wpm, never()).restoreWindowsProfile(anyInt());
            verify(dm, never()).restoreDialogStates();
        }
    }

    @Test
    void testTryRestoreAppState_ProfileExists_UserCloseDialog() throws Exception {
        Path dir = Paths.get("window_profiles");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("testprofile1.state.bin"));

        AppStateRestorer restorer = new AppStateRestorer(frame, messages, gsm, wpm, dm);

        try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mockJOP.when(() -> JOptionPane.showOptionDialog(
                            any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(-1);

            restorer.tryRestoreAppState();

            verify(wpm, never()).restoreWindowsProfile(anyInt());
            verify(dm, never()).restoreDialogStates();
        }
    }
}