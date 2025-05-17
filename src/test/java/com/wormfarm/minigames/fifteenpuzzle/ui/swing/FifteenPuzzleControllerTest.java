package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.util.SoundUtils;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ResourceBundle;

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
    ArgumentCaptor<MouseAdapter> mouseCaptor;

    @BeforeEach
    void setUp() {
        logic = spy(new ClassicFifteenPuzzleLogic(4));
        visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        parent = new JPanel();
        stats = new WormStats(0);
        statsManager = spy(new WormStatsManager(stats));
        mouseCaptor = ArgumentCaptor.forClass(MouseAdapter.class);

        // Capture MouseAdapter instead of using getMouseListeners()
        doNothing().when(visualizer).addMouseListener(mouseCaptor.capture());

        controller = new FifteenPuzzleController(logic, visualizer, parent, statsManager, null);
    }

    private void setBoard(ClassicFifteenPuzzleLogic logic, int[][] board) {
        try {
            Field f = ClassicFifteenPuzzleLogic.class.getDeclaredField("board");
            f.setAccessible(true);
            f.set(logic, board);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testClickOnMovableTile_AnimatesMoveAndPlaysSound() {
        int[][] board = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };
        setBoard(logic, board);
        visualizer.setSize(200, 200);

        MouseAdapter adapter = mouseCaptor.getValue();

        try (var mocked = mockStatic(SoundUtils.class)) {
            MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 2 * 50 + 1, 3 * 50 + 1, 1, false);
            adapter.mouseClicked(e);

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

        MouseAdapter adapter = mouseCaptor.getValue();

        try (var mocked = mockStatic(SoundUtils.class)) {
            MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
            adapter.mouseClicked(e);

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

        MouseAdapter adapter = mouseCaptor.getValue();

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1000, 1000, 1, false);
        adapter.mouseClicked(e);

        verify(logic, never()).moveTile(anyInt(), anyInt());
        verify(visualizer, never()).animateMove(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(visualizer, never()).animateShake(anyInt());
    }

    @Test
    void testClickWhenGameOverDoesNothing() throws Exception {
        Field f = FifteenPuzzleController.class.getDeclaredField("gameOver");
        f.setAccessible(true);
        f.set(controller, true);
        visualizer.setSize(200, 200);

        MouseAdapter adapter = mouseCaptor.getValue();

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
        adapter.mouseClicked(e);

        verify(logic, never()).moveTile(anyInt(), anyInt());
        verify(visualizer, never()).animateMove(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(visualizer, never()).animateShake(anyInt());
    }

    @Test
    void testClickWhileAnimatingDoesNothing() {
        when(visualizer.isAnimating()).thenReturn(true);
        visualizer.setSize(200, 200);

        MouseAdapter adapter = mouseCaptor.getValue();

        MouseEvent e = new MouseEvent(visualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1, false);
        adapter.mouseClicked(e);

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
        FifteenPuzzleFrame frame = mock(FifteenPuzzleFrame.class);
        when(frame.getMessages()).thenReturn(ResourceBundle.getBundle("com.wormfarm.gui.messages"));
        ArgumentCaptor<MouseAdapter> winMouseCaptor = ArgumentCaptor.forClass(MouseAdapter.class);
        doNothing().when(visualizer).addMouseListener(winMouseCaptor.capture());
        FifteenPuzzleController controller = new FifteenPuzzleController(logic, visualizer, parent, statsManager, frame);

        try (var mockedSound = mockStatic(SoundUtils.class);
             var mockedOptionPane = mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(0);

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
    void testHandleWinOnEdt_HandlesReturnToAdventureAndClosesInternalFrameAndIncrementsCoins() {
        JInternalFrame dlg = mock(JInternalFrame.class);
        FifteenPuzzleFrame frame = mock(FifteenPuzzleFrame.class);
        when(frame.getMessages()).thenReturn(ResourceBundle.getBundle("com.wormfarm.gui.messages"));

        ClassicFifteenPuzzleLogic logic = spy(new ClassicFifteenPuzzleLogic(4));
        FifteenPuzzleVisualizer visualizer = spy(new FifteenPuzzleVisualizer(4, 50, logic));
        visualizer.setSize(200, 200);
        WormStats stats = new WormStats(0);
        WormStatsManager statsManager = spy(new WormStatsManager(stats));
        ArgumentCaptor<MouseAdapter> winMouseCaptor = ArgumentCaptor.forClass(MouseAdapter.class);
        doNothing().when(visualizer).addMouseListener(winMouseCaptor.capture());
        FifteenPuzzleController ctrl = new FifteenPuzzleController(logic, visualizer, dlg, statsManager, frame);

        try (var mockedSound = mockStatic(SoundUtils.class);
             var mockedOptionPane = mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(1);
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
        ArgumentCaptor<MouseAdapter> mouseCaptor = ArgumentCaptor.forClass(MouseAdapter.class);
        doNothing().when(visualizer).addMouseListener(mouseCaptor.capture());
        FifteenPuzzleController ctrl = new FifteenPuzzleController(logic, visualizer, parent, statsManager, null);

        // Найти PuzzleEventListener через рефлексию
        PuzzleEventListener listener = null;
        try {
            Field listenersField = ClassicFifteenPuzzleLogic.class.getDeclaredField("listeners");
            listenersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<PuzzleEventListener> listeners = (java.util.List<PuzzleEventListener>) listenersField.get(logic);
            for (Object obj : listeners) {
                if (obj instanceof PuzzleEventListener) listener = (PuzzleEventListener) obj;
            }
        } catch (Exception ignored) {}
        assertNotNull(listener, "Listener must be attached");
        listener.onMove(1, 1, true);
        verify(visualizer, never()).setBoard(any());
        verify(visualizer, never()).repaint();
    }
}