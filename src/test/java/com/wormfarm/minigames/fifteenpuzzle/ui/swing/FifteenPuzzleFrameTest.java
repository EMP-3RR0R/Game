package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.junit.jupiter.api.Assertions.*;

class FifteenPuzzleFrameTest {

    FifteenPuzzleFrame frame;

    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> frame = new FifteenPuzzleFrame());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
    }

    @Test
    void testFrameInitialization() {
        assertNotNull(frame);
        assertEquals("puzzle.title", frame.getTitleKey());
        assertFalse(frame.isResizable());
        assertEquals(new Dimension(420, 520), frame.getPreferredSize());
        assertEquals(new Dimension(420, 520), frame.getMinimumSize());
        assertEquals(new Dimension(420, 520), frame.getMaximumSize());

        assertNotNull(getField(frame, "visualizer"));
        assertNotNull(getField(frame, "controller"));
        assertNotNull(getField(frame, "logic"));
    }

    @Test
    void testTimerAndMovesLabelsUpdate() throws Exception {
        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        JLabel movesLabel = (JLabel) getField(frame, "movesLabel");

        var updateInfo = frame.getClass().getDeclaredMethod("updateInfo");
        updateInfo.setAccessible(true);
        updateInfo.invoke(frame);

        assertTrue(timerLabel.getText().startsWith("Время: "));
        assertTrue(movesLabel.getText().matches("Ходы: \\d+"));
    }

    @Test
    void testResetButtonResetsBoardAndUpdatesLabels() throws Exception {
        JButton resetButton = findButton(frame, "Сброс");
        assertNotNull(resetButton);
        ClassicFifteenPuzzleLogic logic = (ClassicFifteenPuzzleLogic) getField(frame, "logic");

        logic.moveTile(3,2);

        SwingUtilities.invokeAndWait(resetButton::doClick);

        JLabel movesLabel = (JLabel) getField(frame, "movesLabel");
        assertEquals("Ходы: 0", movesLabel.getText());
        assertFalse(logic.isSolved());
    }

    @Test
    void testSolveButtonSolvesBoardAndUpdatesLabels() throws Exception {
        JButton solveButton = findButton(frame, "Решить автоматически");
        assertNotNull(solveButton);
        ClassicFifteenPuzzleLogic logic = (ClassicFifteenPuzzleLogic) getField(frame, "logic");

        logic.moveTile(3,2);
        assertFalse(logic.isSolved());

        SwingUtilities.invokeAndWait(solveButton::doClick);

        assertTrue(logic.isSolved());
        JLabel movesLabel = (JLabel) getField(frame, "movesLabel");
        assertEquals("Ходы: 0", movesLabel.getText());
    }

    @Test
    void testVisualizerIsUpdatedOnResetAndSolve() throws Exception {
        FifteenPuzzleVisualizer visualizer = (FifteenPuzzleVisualizer) getField(frame, "visualizer");
        JButton resetButton = findButton(frame, "Сброс");
        JButton solveButton = findButton(frame, "Решить автоматически");

        FifteenPuzzleVisualizer spyVis = Mockito.spy(visualizer);
        setField(frame, "visualizer", spyVis);

        frame.remove(visualizer);
        frame.add(spyVis, BorderLayout.CENTER);

        SwingUtilities.invokeAndWait(resetButton::doClick);
        Mockito.verify(spyVis, Mockito.atLeastOnce()).resetPuzzleImage();
        Mockito.verify(spyVis, Mockito.atLeastOnce()).setBoard(Mockito.any(int[][].class));
        Mockito.verify(spyVis, Mockito.atLeastOnce()).repaint();

        SwingUtilities.invokeAndWait(solveButton::doClick);
        Mockito.verify(spyVis, Mockito.atLeast(2)).setBoard(Mockito.any(int[][].class));
        Mockito.verify(spyVis, Mockito.atLeast(2)).repaint();
    }

    @Test
    void testTimerRunsAndUpdatesUI() throws Exception {
        Timer uiTimer = (Timer) getField(frame, "uiTimer");
        assertTrue(uiTimer.isRunning());
        for (ActionListener l : uiTimer.getActionListeners()) {
            l.actionPerformed(new java.awt.event.ActionEvent(frame, ActionEvent.ACTION_PERFORMED, "test"));
        }
        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        assertTrue(timerLabel.getText().startsWith("Время: "));
    }

    @Test
    void testDisposeCleansResources() {
        FifteenPuzzleVisualizer visualizer = (FifteenPuzzleVisualizer) getField(frame, "visualizer");
        FifteenPuzzleVisualizer spyVis = Mockito.spy(visualizer);
        setField(frame, "visualizer", spyVis);

        frame.dispose();

        Mockito.verify(spyVis, Mockito.times(1)).clearSprites();
        assertFalse(frame.isDisplayable());
    }

    // Добавлено: тест на setMaximum/setIcon & restore
    @Test
    void testMaximizeAndIconifyRestore() throws Exception {
        frame.setVisible(true);
        frame.setBounds(10, 20, 300, 200);
        frame.setMaximum(true);
        assertTrue(frame.isMaximum());

        frame.setIcon(true);
        assertTrue(frame.isIcon());

        frame.setIcon(false);
        assertFalse(frame.isIcon());

        frame.setMaximum(false);
        assertFalse(frame.isMaximum());
    }

    private Object getField(Object obj, String name) {
        try {
            var f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String name, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JButton findButton(Container c, String text) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton b && text.equals(b.getText())) return b;
            if (comp instanceof Container child) {
                JButton found = findButton(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}