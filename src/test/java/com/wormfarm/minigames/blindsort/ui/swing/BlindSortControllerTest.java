package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Field;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlindSortControllerTest {

    private BlindSortController controller;

    @Mock
    private BlindSortLogic mockGame;
    @Mock
    private BlindSortVisualizer mockVisualizer;
    @Mock
    private WormStatsManager mockStatsManager;
    @Mock
    private Component mockParentComponent;
    @Mock
    private ResourceBundle mockMessages;

    private ArgumentCaptor<MouseAdapter> mouseAdapterCaptor;
    private ArgumentCaptor<MouseMotionAdapter> mouseMotionAdapterCaptor;
    private ArgumentCaptor<BlindSortEventListener> gameEventListenerCaptor;

    private MouseAdapter capturedMouseAdapter;
    private MouseMotionAdapter capturedMouseMotionAdapter;
    private BlindSortEventListener capturedGameEventListener;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockMessages.getString(anyString())).thenReturn("test_string");
        when(mockMessages.getString("blindsort.next_stage")).thenReturn("Next Stage");
        when(mockMessages.getString("blindsort.reset")).thenReturn("Reset");
        when(mockMessages.getString("blindsort.win.title")).thenReturn("Win Title");
        when(mockMessages.getString("blindsort.lose.title")).thenReturn("Lose Title");
        when(mockMessages.getString("blindsort.win.message")).thenReturn("Win Message");
        when(mockMessages.getString("blindsort.lose.message")).thenReturn("Lose Message");
        when(mockMessages.getString("blindsort.win.play_again")).thenReturn("Play Again");
        when(mockMessages.getString("blindsort.lose.try_again")).thenReturn("Try Again");
        when(mockMessages.getString("blindsort.win.return")).thenReturn("Return");

        when(mockVisualizer.getCubeHeight()).thenReturn(50);
        when(mockVisualizer.getRowSpacing()).thenReturn(10);
        when(mockVisualizer.getLabelOffset()).thenReturn(20);
        when(mockVisualizer.getRowPadding()).thenReturn(10);
        when(mockVisualizer.getCubeWidth()).thenReturn(40);
        when(mockVisualizer.getCubeSpacing()).thenReturn(5);
        when(mockGame.getNumbersCopy(ArrayOwner.PLAYER)).thenReturn(new int[][]{{1}, {2}, {3}, {4}, {5}});

        mouseAdapterCaptor = ArgumentCaptor.forClass(MouseAdapter.class);
        mouseMotionAdapterCaptor = ArgumentCaptor.forClass(MouseMotionAdapter.class);
        gameEventListenerCaptor = ArgumentCaptor.forClass(BlindSortEventListener.class);


        controller = new BlindSortController(mockGame, mockVisualizer, mockParentComponent, mockStatsManager, mockMessages);

        verify(mockVisualizer).addMouseListener(mouseAdapterCaptor.capture());
        verify(mockVisualizer).addMouseMotionListener(mouseMotionAdapterCaptor.capture());
        verify(mockGame).addEventListener(gameEventListenerCaptor.capture());

        capturedMouseAdapter = mouseAdapterCaptor.getValue();
        capturedMouseMotionAdapter = mouseMotionAdapterCaptor.getValue();
        capturedGameEventListener = gameEventListenerCaptor.getValue();
    }

    @Test
    void constructorSetsUpVisualizerAndListeners() {
        verify(mockVisualizer).setControllerCallback(controller);
        assertNotNull(capturedMouseAdapter);
        assertNotNull(capturedMouseMotionAdapter);
        assertNotNull(capturedGameEventListener);
    }

    @Test
    void mouseClickedHandlesPlayerCubeSelection() throws Exception {
        MouseEvent clickEvent = createMouseEvent(MouseEvent.MOUSE_CLICKED, 100, 40);
        when(mockGame.isPaused()).thenReturn(false);
        when(mockGame.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(false);
        when(mockGame.isGameActive()).thenReturn(true);

        capturedMouseAdapter.mouseClicked(clickEvent);

        Integer selectedIndex = (Integer) getField(controller, "selectedIndexForPlayer");
        assertNotNull(selectedIndex);
        assertEquals(2, selectedIndex);
        verify(mockVisualizer).setPlayerSelectedIndex(selectedIndex);
    }

    @Test
    void mouseClickedHandlesPlayerCubeDeselection() throws Exception {
        MouseEvent clickEvent = createMouseEvent(MouseEvent.MOUSE_CLICKED, 100, 40);

        setField(controller, "selectedIndexForPlayer", 2);
        when(mockGame.isPaused()).thenReturn(false);
        when(mockGame.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(false);
        when(mockGame.isGameActive()).thenReturn(true);

        capturedMouseAdapter.mouseClicked(clickEvent);

        assertNull(getField(controller, "selectedIndexForPlayer"));
        verify(mockVisualizer).setPlayerSelectedIndex(null);
    }

    @Test
    void mouseClickedHandlesPlayerCubeSwapSuccess() throws Exception {
        MouseEvent clickEvent2 = createMouseEvent(MouseEvent.MOUSE_CLICKED, 150, 40);

        setField(controller, "selectedIndexForPlayer", 2);
        when(mockGame.isPaused()).thenReturn(false);
        when(mockGame.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(false);
        when(mockGame.isGameActive()).thenReturn(true);
        when(mockGame.swapNumbers(eq(2), eq(3))).thenReturn(true);

        capturedMouseAdapter.mouseClicked(clickEvent2);

        verify(mockGame).swapNumbers(eq(2), eq(3));
        verify(mockVisualizer).setPlayerSelectedIndex(null);
        verify(mockGame).setAnimationInProgress(true, ArrayOwner.PLAYER);
        verify(mockVisualizer).startPlayerSwapAnimation(eq(2), eq(3));
        assertNull(getField(controller, "selectedIndexForPlayer"));
    }

    @Test
    void mouseClickedHandlesPlayerCubeSwapFailure() throws Exception {
        MouseEvent clickEvent2 = createMouseEvent(MouseEvent.MOUSE_CLICKED, 150, 40);

        setField(controller, "selectedIndexForPlayer", 2);
        when(mockGame.isPaused()).thenReturn(false);
        when(mockGame.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(false);
        when(mockGame.isGameActive()).thenReturn(true);
        when(mockGame.swapNumbers(eq(2), eq(3))).thenReturn(false);

        capturedMouseAdapter.mouseClicked(clickEvent2);

        verify(mockGame).swapNumbers(eq(2), eq(3));
        verify(mockVisualizer).setPlayerSelectedIndex(null);
        verify(mockGame, never()).setAnimationInProgress(anyBoolean(), any(ArrayOwner.class));
        verify(mockVisualizer, never()).startPlayerSwapAnimation(anyInt(), anyInt());
        assertNull(getField(controller, "selectedIndexForPlayer"));
    }

    @Test
    void mouseClickedDoesNothingWhenGameOverOrPausedOrAnimating() throws Exception {
        MouseEvent clickEvent = createMouseEvent(MouseEvent.MOUSE_CLICKED, 100, 40);

        setField(controller, "gameOver", true);
        capturedMouseAdapter.mouseClicked(clickEvent);
        verify(mockGame, never()).isPaused();
        setField(controller, "gameOver", false);

        when(mockGame.isPaused()).thenReturn(true);
        capturedMouseAdapter.mouseClicked(clickEvent);
        verify(mockGame).isPaused();
        verify(mockGame, never()).isAnimationInProgress(ArrayOwner.PLAYER);
        when(mockGame.isPaused()).thenReturn(false);

        when(mockGame.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(true);
        capturedMouseAdapter.mouseClicked(clickEvent);
        verify(mockGame, Mockito.atLeast(1)).isAnimationInProgress(ArrayOwner.PLAYER);
        verify(mockGame, never()).getNumbersCopy(ArrayOwner.PLAYER);
    }

    @Test
    void mouseMovedHandlesPlayerHover() throws Exception {
        MouseEvent hoverEvent = createMouseEvent(MouseEvent.MOUSE_MOVED, 100, 40);
        capturedMouseMotionAdapter.mouseMoved(hoverEvent);

        verify(mockVisualizer).handlePlayerHover(eq(2));
    }

    @Test
    void mouseMovedHandlesNoHover() throws Exception {
        MouseEvent hoverEvent = createMouseEvent(MouseEvent.MOUSE_MOVED, 10, 10);
        capturedMouseMotionAdapter.mouseMoved(hoverEvent);

        verify(mockVisualizer).handlePlayerHover(eq(-1));
    }

    @Test
    void handlePlayerSwapAnimationFinishedResetsState() throws Exception {
        setField(controller, "selectedIndexForPlayer", 1);

        controller.handlePlayerSwapAnimationFinished();

        assertNull(getField(controller, "selectedIndexForPlayer"));
        verify(mockVisualizer).setPlayerSelectedIndex(null);
        verify(mockVisualizer).updateNumbersCache(ArrayOwner.PLAYER);
        verify(mockGame).setAnimationInProgress(false, ArrayOwner.PLAYER);
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

    private MouseEvent createMouseEvent(int id, int x, int y) {
        return new MouseEvent(new JPanel(), id, System.currentTimeMillis(), 0, x, y, 1, false);
    }
}