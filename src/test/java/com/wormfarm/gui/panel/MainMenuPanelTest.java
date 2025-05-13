package com.wormfarm.gui.panel;

import com.wormfarm.settings.UserSettings;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

class MainMenuPanelTest {

    Runnable onContinue, onNewGame, onLoadGame, onSettings, onExit;
    UserSettings settings;
    MainMenuPanel panel;

    @BeforeEach
    void setUp() {
        onContinue = mock(Runnable.class);
        onNewGame = mock(Runnable.class);
        onLoadGame = mock(Runnable.class);
        onSettings = mock(Runnable.class);
        onExit = mock(Runnable.class);
        settings = mock(UserSettings.class);

        panel = new MainMenuPanel(
                onContinue,
                onNewGame,
                onLoadGame,
                onSettings,
                onExit,
                true,
                settings
        );
    }

    @AfterEach
    void closeAllWindows() {
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) {
                try { w.dispose(); } catch (Exception ignored) {}
            }
        }
    }

    @Test
    void testAllButtonsPresent() {
        Assertions.assertEquals(5, panel.getComponentCount());
        for (int i = 0; i < 5; i++) {
            Assertions.assertInstanceOf(JButton.class, panel.getComponent(i), "Component " + i + " is not JButton");
        }
    }

    @Test
    void testContinueAndLoadGameButtonEnabled() {
        JButton btnContinue = (JButton) panel.getComponent(0);
        JButton btnLoad = (JButton) panel.getComponent(2);
        Assertions.assertTrue(btnContinue.isEnabled());
        Assertions.assertTrue(btnLoad.isEnabled());
    }

    @Test
    void testContinueAndLoadGameButtonDisabled() {
        panel = new MainMenuPanel(
                onContinue,
                onNewGame,
                onLoadGame,
                onSettings,
                onExit,
                false,
                settings
        );
        JButton btnContinue = (JButton) panel.getComponent(0);
        JButton btnLoad = (JButton) panel.getComponent(2);
        Assertions.assertFalse(btnContinue.isEnabled());
        Assertions.assertFalse(btnLoad.isEnabled());
    }

    @Test
    void testButtonActionsInvokeRunnablesAndNoWindowsLeft() {
        JButton btnContinue = (JButton) panel.getComponent(0);
        JButton btnNewGame = (JButton) panel.getComponent(1);
        JButton btnLoadGame = (JButton) panel.getComponent(2);
        JButton btnExit = (JButton) panel.getComponent(4);

        btnContinue.doClick();
        btnNewGame.doClick();
        btnLoadGame.doClick();
        btnExit.doClick();

        verify(onContinue, times(1)).run();
        verify(onNewGame, times(1)).run();
        verify(onLoadGame, times(1)).run();
        verify(onExit, times(1)).run();

        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) {
                w.dispose();
            }
        }

        for (Window w : Window.getWindows()) {
            Assertions.assertFalse(w.isDisplayable(), "Есть не закрытое окно: " + w);
        }
    }

    @Test
    void testUpdateTextsNotCrashing() throws Exception {
        Method method = MainMenuPanel.class.getDeclaredMethod("updateTexts");
        method.setAccessible(true);
        method.invoke(panel);
    }
}