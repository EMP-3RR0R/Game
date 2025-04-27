package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FifteenPuzzleControllerTest {

    ClassicFifteenPuzzleLogic logic;
    FifteenPuzzleVisualizer visualizer;
    JPanel parent;
    WormStats stats;
    WormStatsManager statsManager;
    FifteenPuzzleController controller;

    @BeforeEach
    void setUp() {
        logic = spy(new ClassicFifteenPuzzleLogic(4));
        visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        parent = new JPanel();
        stats = new WormStats(0);
        statsManager = spy(new WormStatsManager(stats));
        controller = new FifteenPuzzleController(logic, visualizer, parent, statsManager);
    }

    /** Вспомогательная функция для прямой подмены содержимого доски */
    private void setBoard(ClassicFifteenPuzzleLogic logic, int[][] board) {
        try {
            var f = ClassicFifteenPuzzleLogic.class.getDeclaredField("board");
            f.setAccessible(true);
            f.set(logic, board);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testClickOnMovableTile_AnimatesMoveAndPlaysSound() {
        // (3,3) = 0; (3,2) = 15. Клик на (3,2) должен сдвинуть 15 вправо
        int[][] board = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
        setBoard(logic, board);
        visualizer.setSize(200, 200);

        try (var mocked = Mockito.mockStatic(SoundUtils.class)) {
            MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 2 * 50 + 1, 3 * 50 + 1, 1, false);
            for (var l : visualizer.getMouseListeners()) l.mouseClicked(e);

            verify(logic).moveTile(3, 2);
            verify(visualizer).animateMove(eq(15), eq(3), eq(2), eq(3), eq(3), any());
            mocked.verify(() -> SoundUtils.playSound("/sounds/click.wav"));
        }
    }

    @Test
    void testClickOnNonMovableTile_AnimatesShakeAndPlaysSound() {
        int[][] board = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
        setBoard(logic, board);
        visualizer.setSize(200, 200);

        try (var mocked = Mockito.mockStatic(SoundUtils.class)) {
            MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
            for (var l : visualizer.getMouseListeners()) l.mouseClicked(e);

            verify(logic).moveTile(0, 0);
            verify(visualizer).animateShake(1);
            mocked.verify(() -> SoundUtils.playSound("/sounds/click.wav"));
        }
    }

    @Test
    void testClickOutsideFieldDoesNothing() {
        int[][] board = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
        setBoard(logic, board);
        visualizer.setSize(200, 200);

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1000, 1000, 1, false);
        for (var l : visualizer.getMouseListeners()) l.mouseClicked(e);

        verify(logic, never()).moveTile(anyInt(), anyInt());
        verify(visualizer, never()).animateMove(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(visualizer, never()).animateShake(anyInt());
    }

    @Test
    void testClickWhenGameOverDoesNothing() throws Exception {
        // Вручную выставляем gameOver = true
        var f = FifteenPuzzleController.class.getDeclaredField("gameOver");
        f.setAccessible(true);
        f.set(controller, true);
        visualizer.setSize(200, 200);

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
        for (var l : visualizer.getMouseListeners()) l.mouseClicked(e);

        verify(logic, never()).moveTile(anyInt(), anyInt());
        verify(visualizer, never()).animateMove(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(visualizer, never()).animateShake(anyInt());
    }

    @Test
    void testClickWhileAnimatingDoesNothing() {
        when(visualizer.isAnimating()).thenReturn(true);
        visualizer.setSize(200, 200);

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
        for (var l : visualizer.getMouseListeners()) l.mouseClicked(e);

        verify(logic, never()).moveTile(anyInt(), anyInt());
        verify(visualizer, never()).animateMove(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(visualizer, never()).animateShake(anyInt());
    }

    @Test
    void testHandleWinOnEdt_StartsDialogAndHandlesPlayAgainAndIncrementsCoins() {
        ClassicFifteenPuzzleLogic logic = spy(new ClassicFifteenPuzzleLogic(4));
        FifteenPuzzleVisualizer visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        visualizer.setSize(200, 200);
        JPanel parent = new JPanel();
        WormStats stats = new WormStats(0);
        WormStatsManager statsManager = spy(new WormStatsManager(stats));
        FifteenPuzzleController controller = new FifteenPuzzleController(logic, visualizer, parent, statsManager);

        try (var mockedSound = Mockito.mockStatic(SoundUtils.class);
             var mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(0); // "Сыграть ещё!"

            controller.handleWinOnEdt();

            mockedSound.verify(() -> SoundUtils.playSound("/sounds/win.wav"));
            verify(visualizer, atLeastOnce()).resetPuzzleImage();
            verify(visualizer, atLeastOnce()).setBoard(any(int[][].class));
            verify(visualizer, atLeastOnce()).repaint();
            verify(statsManager, atLeastOnce()).addCoins(10);
            assertEquals(10, stats.getWormCoins());
        }
    }

    @Test
    void testHandleWinOnEdt_HandlesReturnToAdventureAndClosesDialogAndIncrementsCoins() {
        JDialog dlg = mock(JDialog.class);
        FifteenPuzzleFrame frame = mock(FifteenPuzzleFrame.class);
        when(frame.getParentDialog()).thenReturn(dlg);

        ClassicFifteenPuzzleLogic logic = spy(new ClassicFifteenPuzzleLogic(4));
        FifteenPuzzleVisualizer visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        visualizer.setSize(200, 200);
        WormStats stats = new WormStats(0);
        WormStatsManager statsManager = spy(new WormStatsManager(stats));
        FifteenPuzzleController ctrl = new FifteenPuzzleController(logic, visualizer, frame, statsManager);

        try (var mockedSound = Mockito.mockStatic(SoundUtils.class);
             var mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(1); // "Вернуться к приключениям!"
            ctrl.handleWinOnEdt();
            verify(dlg).dispose();
            verify(statsManager, atLeastOnce()).addCoins(10);
            assertEquals(10, stats.getWormCoins());
        }
    }

    @Test
    void testOnMoveListenerDoesNotUpdateVisualizer() {
        ClassicFifteenPuzzleLogic logic = new ClassicFifteenPuzzleLogic(4);
        FifteenPuzzleVisualizer visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        JPanel parent = new JPanel();
        WormStatsManager statsManager = mock(WormStatsManager.class);
        FifteenPuzzleController ctrl = new FifteenPuzzleController(logic, visualizer, parent, statsManager);

        // Ищем listener в logic
        com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener listener = null;
        for (var l : logic.getClass().getDeclaredFields()) {
            if (l.getType().getName().contains("List")) {
                l.setAccessible(true);
                try {
                    var list = (java.util.List<?>) l.get(logic);
                    for (var obj : list) {
                        if (obj instanceof com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener) listener = (com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener) obj;
                    }
                } catch (Exception ignored) {}
            }
        }
        assertNotNull(listener, "Listener must be attached");
        listener.onMove(1, 1, true);
        verify(visualizer, never()).setBoard(any());
        verify(visualizer, never()).repaint();
    }
}