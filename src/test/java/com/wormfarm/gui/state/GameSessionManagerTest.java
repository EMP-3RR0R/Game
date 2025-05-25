package com.wormfarm.gui.state;

import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.settings.UserSettings;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static org.mockito.Mockito.*;

class GameSessionManagerTest {
    JFrame frame;
    UserSettings settings;
    ResourceBundle messages;
    JDesktopPane desktopPane;
    GameSessionManager gsm;

    @BeforeEach
    void setUp() {
        frame = mock(JFrame.class);
        settings = mock(UserSettings.class);
        messages = mock(ResourceBundle.class);
        desktopPane = new JDesktopPane();

        when(messages.getString(anyString())).thenAnswer(inv -> inv.getArguments()[0] + "_msg");

        gsm = new GameSessionManager(frame, settings, messages, desktopPane);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) w.dispose();
        }
        new File("app.state.bin").delete();
    }

    @Test
    void testCanActivateEventFlag() {
        Assertions.assertTrue(gsm.canActivateEvent());
        gsm.setRestoredResumableWindows(1); // исправление
        gsm.onResumableWindowClosed();
        Assertions.assertFalse(gsm.canActivateEvent());
    }

    @Test
    void testShutdownCurrentMapPanel_DoesNotCrash() {
        gsm.shutdownCurrentMapPanel();
    }

    @Test
    void testShowMainMenu_DoesNotCrash() {
        try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
            mockJOP.when(() -> JOptionPane.showMessageDialog(any(), any())).then(inv -> null);
            gsm.showMainMenu();
        }
    }

    @Test
    void testContinueGame_WithSaves_CallsSetContentPane() {
        try (var mockSaveManager = org.mockito.Mockito.mockStatic(WormSaveManager.class)) {
            mockSaveManager.when(WormSaveManager::listSaves).thenReturn(List.of("save1"));
            try (var mockJOP = org.mockito.Mockito.mockStatic(JOptionPane.class)) {
                mockJOP.when(() -> JOptionPane.showMessageDialog(any(), any())).then(inv -> null);

                gsm.continueGame();
                verify(frame, atLeastOnce()).setContentPane(any());
            }
        }
    }

    @Test
    void testSaveAndRestoreAppState() {
        gsm.saveAppState();
        Assertions.assertTrue(new File("app.state.bin").exists());
        gsm.restoreAppState();
    }
}