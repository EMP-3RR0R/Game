package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.settings.UserSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

class BlindSortFrameTest {

    private BlindSortFrame frame;
    private ResourceBundle messages;

    @Mock
    private WormStatsManager mockWormStatsManager;
    @Mock
    private UserSettings mockUserSettings;
    @Mock
    private GameSessionManager mockGameSessionManager;

    private BlindSortLogic spyLogic;
    private BlindSortVisualizer spyVisualizer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        spyLogic = spy(new BlindSortLogic(5));
        spyVisualizer = spy(new BlindSortVisualizer(spyLogic, 800, 600));

        Locale.setDefault(new Locale("en", "US"));
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", Locale.getDefault());

        SwingUtilities.invokeAndWait(() -> {
            frame = new BlindSortFrame(mockWormStatsManager, mockUserSettings, false, mockGameSessionManager);
            setField(frame, "game", spyLogic);
            setField(frame, "visualizer", spyVisualizer);
            setField(frame, "messages", messages);
        });
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
    }

    @Test
    void testFrameInitialization() throws Exception {
        assertNotNull(frame);
        assertTrue(frame.isClosable());
        assertTrue(frame.isMaximizable());
        assertTrue(frame.isResizable());
        assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, frame.getDefaultCloseOperation());
        assertEquals(new Dimension(800, 600), frame.getPreferredSize());

        assertNotNull(getField(frame, "visualizer"));
        assertNotNull(getField(frame, "game"));
        assertNotNull(getField(frame, "uiTimer"));
        assertNotNull(getField(frame, "timerLabel"));
        assertNotNull(getField(frame, "swapCountLabel"));
        assertNotNull(getField(frame, "stageLabel"));
    }

    @Test
    void testNextStageButtonAdvancesStage() throws Exception {
        JButton nextStageButton = findButton(frame, messages.getString("blindsort.next_stage"));

        JLabel stageLabel = (JLabel) getField(frame, "stageLabel");

        when(spyLogic.isGameActive()).thenReturn(true);
        when(spyLogic.getVisibleDigits()).thenReturn(1);
        when(spyLogic.isSorted(BlindSortLogic.ArrayOwner.PLAYER)).thenReturn(true);
        when(spyLogic.getVisibleDigits()).thenReturn(2);
        invokeMethod(frame, "updateInfo");

        assertEquals(messages.getString("blindsort.stage") + ": 2", stageLabel.getText());
    }

    @Test
    void testResetButtonResetsGameAndLabels() throws Exception {
        JButton resetButton = findButton(frame, messages.getString("blindsort.reset"));

        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        JLabel swapCountLabel = (JLabel) getField(frame, "swapCountLabel");
        JLabel stageLabel = (JLabel) getField(frame, "stageLabel");

        when(spyLogic.getSwapCount()).thenReturn(10);
        when(spyLogic.getElapsedTime()).thenReturn(60000L);
        when(spyLogic.isFinalStage(BlindSortLogic.ArrayOwner.PLAYER)).thenReturn(false);
        when(spyLogic.getVisibleDigits()).thenReturn(3);
        invokeMethod(frame, "updateInfo");

        assertEquals(messages.getString("blindsort.moves") + ": 10", swapCountLabel.getText());
        assertTrue(timerLabel.getText().contains("01:00"));
        assertEquals(messages.getString("blindsort.stage") + ": 3", stageLabel.getText());

        when(spyLogic.getSwapCount()).thenReturn(0);
        when(spyLogic.getElapsedTime()).thenReturn(0L);
        when(spyLogic.getVisibleDigits()).thenReturn(1);
        when(spyLogic.isFinalStage(BlindSortLogic.ArrayOwner.PLAYER)).thenReturn(false);
        invokeMethod(frame, "updateInfo");

        assertEquals(messages.getString("blindsort.moves") + ": 0", swapCountLabel.getText());
        assertEquals(messages.getString("blindsort.stage") + ": 1", stageLabel.getText());
        assertEquals(messages.getString("blindsort.time") + ": 00:00", timerLabel.getText());
    }

    @Test
    void testUpdateInfoUpdatesLabelsCorrectly() throws Exception {
        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        JLabel swapCountLabel = (JLabel) getField(frame, "swapCountLabel");
        JLabel stageLabel = (JLabel) getField(frame, "stageLabel");

        when(spyLogic.getElapsedTime()).thenReturn(123456L);
        when(spyLogic.getSwapCount()).thenReturn(25);
        when(spyLogic.isFinalStage()).thenReturn(false);
        when(spyLogic.getVisibleDigits()).thenReturn(4);
        when(spyLogic.isGameActive()).thenReturn(true);

        SwingUtilities.invokeAndWait(() -> frame.updateInfo());

        assertEquals("Time: 02:03", timerLabel.getText());
        assertEquals("Moves: 25", swapCountLabel.getText());
        assertEquals("Stage: 4", stageLabel.getText());
        verify(spyVisualizer).repaint();

        when(spyLogic.isFinalStage()).thenReturn(true);
        SwingUtilities.invokeAndWait(() -> frame.updateInfo());
        assertEquals("Stage: Final", stageLabel.getText());

        Timer uiTimer = (Timer) getField(frame, "uiTimer");
        when(spyLogic.isGameActive()).thenReturn(false);
        SwingUtilities.invokeAndWait(() -> frame.updateInfo());
        assertFalse(uiTimer.isRunning());
    }

    @Test
    void testTimerRunsAndUpdatesUI() throws Exception {
        Timer uiTimer = (Timer) getField(frame, "uiTimer");

        assertTrue(uiTimer.isRunning());

        for (ActionListener l : uiTimer.getActionListeners()) {
            SwingUtilities.invokeAndWait(() -> l.actionPerformed(new ActionEvent(uiTimer, ActionEvent.ACTION_PERFORMED, "")));
        }

        verify(spyLogic, atLeastOnce()).getElapsedTime();
        verify(spyLogic, atLeastOnce()).getSwapCount();
        verify(spyLogic, atLeastOnce()).getVisibleDigits();
        verify(spyVisualizer, atLeastOnce()).repaint();
    }

    @Test
    void testPauseGameLogic() throws Exception {
        SwingUtilities.invokeAndWait(() -> frame.pauseGame());

        verify(spyLogic).pauseGame();
    }

    @Test
    void testResumeGameLogic() throws Exception {
        SwingUtilities.invokeAndWait(() -> frame.resumeGame());

        verify(spyLogic).resumeGame();
    }

    @Test
    void testEndGameCleansResources() throws Exception {
        Timer uiTimer = (Timer) getField(frame, "uiTimer");

        SwingUtilities.invokeAndWait(() -> frame.endGame());

        assertFalse(uiTimer.isRunning());
    }

    @Test
    void testDisposeCleansResources() throws Exception {
        Timer uiTimer = (Timer) getField(frame, "uiTimer");

        SwingUtilities.invokeAndWait(() -> frame.dispose());

        assertFalse(uiTimer.isRunning());
        verify(spyLogic).stopAI();
    }

    private Object getField(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field: " + name, e);
        }
    }

    private void setField(Object obj, String name, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + name, e);
        }
    }

    private void invokeMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }
            Method m = obj.getClass().getDeclaredMethod(methodName, argTypes);
            m.setAccessible(true);
            m.invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private JButton findButton(Container c, String text) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton b && text.equals(b.getText())) {
                return b;
            }
            if (comp instanceof Container child) {
                JButton found = findButton(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}