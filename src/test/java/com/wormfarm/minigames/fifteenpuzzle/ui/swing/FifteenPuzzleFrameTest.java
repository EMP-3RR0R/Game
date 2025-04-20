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
        // Для Swing-тестов запускаем в EDT
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

        // Проверяем наличие визуализатора и контроллера (reflection)
        assertNotNull(getField(frame, "visualizer"));
        assertNotNull(getField(frame, "controller"));
        assertNotNull(getField(frame, "logic"));
    }

    @Test
    void testTimerAndMovesLabelsUpdate() throws Exception {
        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        JLabel movesLabel = (JLabel) getField(frame, "movesLabel");

        // Принудительно вызываем updateInfo()
        var updateInfo = frame.getClass().getDeclaredMethod("updateInfo");
        updateInfo.setAccessible(true);
        updateInfo.invoke(frame);

        // Текст должен быть в формате времени и ходов
        assertTrue(timerLabel.getText().startsWith("Время: "));
        assertTrue(movesLabel.getText().matches("Ходы: \\d+"));
    }

    @Test
    void testResetButtonResetsBoardAndUpdatesLabels() throws Exception {
        JButton resetButton = findButton(frame, "Сброс");
        assertNotNull(resetButton);
        ClassicFifteenPuzzleLogic logic = (ClassicFifteenPuzzleLogic) getField(frame, "logic");

        // Совершаем ход, чтобы были изменения
        logic.moveTile(3,2);

        // Нажимаем сброс
        SwingUtilities.invokeAndWait(resetButton::doClick);

        // Ожидаем сброса ходов и обновления меток
        JLabel movesLabel = (JLabel) getField(frame, "movesLabel");
        assertEquals("Ходы: 0", movesLabel.getText());
        assertFalse(logic.isSolved()); // после сброса обычно не решено
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

        // Мокаем методы для проверки вызовов
        FifteenPuzzleVisualizer spyVis = Mockito.spy(visualizer);
        setField(frame, "visualizer", spyVis);

        // Повторно добавляем в панель (иначе кнопки вызовут методы на старом объекте)
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
        // Принудительно вызываем ActionListener
        for (ActionListener l : uiTimer.getActionListeners()) {
            l.actionPerformed(new java.awt.event.ActionEvent(frame, ActionEvent.ACTION_PERFORMED, "test"));
        }
        // Проверяем что метки времени обновились
        JLabel timerLabel = (JLabel) getField(frame, "timerLabel");
        assertTrue(timerLabel.getText().startsWith("Время: "));
    }

    @Test
    void testParentDialogSetterAndGetter() {
        JDialog dialog = new JDialog();
        frame.setParentDialog(dialog);
        assertSame(dialog, frame.getParentDialog());
    }

    @Test
    void testDisposeCleansResources() {
        FifteenPuzzleVisualizer visualizer = (FifteenPuzzleVisualizer) getField(frame, "visualizer");
        FifteenPuzzleVisualizer spyVis = Mockito.spy(visualizer);
        setField(frame, "visualizer", spyVis);

        frame.dispose();

        Mockito.verify(spyVis).clearSprites();
        assertFalse(frame.isDisplayable());
    }

    // ----------------- Вспомогательные методы -----------------

    /** Получить приватное поле по имени */
    private Object getField(Object obj, String name) {
        try {
            var f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Установить приватное поле */
    private void setField(Object obj, String name, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Поиск JButton по тексту */
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