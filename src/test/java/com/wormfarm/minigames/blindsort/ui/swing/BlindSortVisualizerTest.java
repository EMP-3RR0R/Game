package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlindSortVisualizerTest {
    private BlindSortVisualizer visualizer;
    private BlindSortLogic gameLogic;
    private BlindSortController controllerCallback;
    private ResourceBundle messages;
    private Graphics2D graphics;

    @BeforeEach
    void setUp() {
        gameLogic = mock(BlindSortLogic.class);
        when(gameLogic.getNumbersCopy()).thenReturn(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}});
        when(gameLogic.getNumbersCopy(ArrayOwner.PLAYER)).thenReturn(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}});
        when(gameLogic.getNumbersCopy(ArrayOwner.FAST_AI)).thenReturn(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}});
        when(gameLogic.getNumbersCopy(ArrayOwner.SLOW_AI)).thenReturn(new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}});
        when(gameLogic.getVisibleDigitsForOwner(any())).thenReturn(1);
        when(gameLogic.isFinalStage(any())).thenReturn(false);
        when(gameLogic.isGameActive()).thenReturn(true);
        when(gameLogic.isPaused()).thenReturn(false);
        when(gameLogic.isAnimationInProgress(any())).thenReturn(false);
        when(gameLogic.getFastAIStrategyName()).thenReturn("bubbleSort");
        when(gameLogic.getSlowAIStrategyName()).thenReturn("insertionSort");

        messages = mock(ResourceBundle.class);
        when(messages.getString("blindsort.player")).thenReturn("Player");
        when(messages.getString("strategy.bubbleSort")).thenReturn("Bubbler");
        when(messages.getString("strategy.insertionSort")).thenReturn("Inserter");

        visualizer = new BlindSortVisualizer(gameLogic, 800, 600);
        visualizer.messages = messages;

        controllerCallback = mock(BlindSortController.class);
        visualizer.setControllerCallback(controllerCallback);

        graphics = mock(Graphics2D.class);
        doNothing().when(graphics).setRenderingHint(any(), any());
        doNothing().when(graphics).setColor(any());
        doNothing().when(graphics).setFont(any());
        doNothing().when(graphics).drawString(anyString(), anyInt(), anyInt());
        doNothing().when(graphics).fillRoundRect(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        doNothing().when(graphics).setStroke(any());
        doNothing().when(graphics).drawRoundRect(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        when(graphics.getFontMetrics()).thenReturn(mock(FontMetrics.class));
    }

    private void completeLiftAnimation(BlindSortVisualizer.AnimationState state) {
        if (state.liftedIndex != null && state.timer.isRunning()) {
            ActionListener listener = state.timer.getActionListeners()[0];
            while (state.liftProgress > 0f && state.liftedIndex != null) {
                listener.actionPerformed(null);
            }
        }
    }

    @Test
    void testInitialization() {
        assertEquals(gameLogic, visualizer.gameLogic, "Game logic should be set");
        assertEquals(messages, visualizer.messages, "Messages should be set");
        assertNotNull(visualizer.animationStates, "Animation states should be initialized");
        assertEquals(3, visualizer.animationStates.size(), "Should have 3 animation states");
        assertTrue(visualizer.animationStates.containsKey(ArrayOwner.PLAYER), "Player animation state should exist");
        assertTrue(visualizer.animationStates.containsKey(ArrayOwner.FAST_AI), "Fast AI animation state should exist");
        assertTrue(visualizer.animationStates.containsKey(ArrayOwner.SLOW_AI), "Slow AI animation state should exist");
        assertEquals(800, visualizer.getPreferredSize().width, "Preferred width should be 800");
        assertEquals(600, visualizer.getPreferredSize().height, "Preferred height should be 600");
        assertTrue(visualizer.getBackground().equals(new Color(40, 44, 52)), "Background color should be set");
        assertNull(visualizer.playerSelectedIndex, "No selected index initially");
        assertNull(visualizer.playerHoveredIndex, "No hovered index initially");

        ArgumentCaptor<BlindSortEventListener> listenerCaptor = ArgumentCaptor.forClass(BlindSortEventListener.class);
        verify(gameLogic).addEventListener(listenerCaptor.capture());
        assertEquals(visualizer, listenerCaptor.getValue(), "Visualizer should be registered as listener");
    }

    @Test
    void testSetControllerCallback() {
        BlindSortController newController = mock(BlindSortController.class);
        visualizer.setControllerCallback(newController);
        assertEquals(newController, visualizer.controllerCallback, "Controller callback should be set");
    }

    @Test
    void testHandlePlayerHoverNormal() {
        visualizer.handlePlayerHover(1);
        assertEquals(1, visualizer.playerHoveredIndex, "Hovered index should be 1");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertEquals(1, state.liftedIndex, "Lift animation should start for index 1");
        assertTrue(state.lifting, "Lift animation should be upward");

        visualizer.handlePlayerHover(2);
        assertEquals(2, visualizer.playerHoveredIndex, "Hovered index should be 2");
        assertEquals(2, state.liftedIndex, "Lift animation should start for index 2");
        assertTrue(state.lifting, "Lift animation should be upward");

        visualizer.handlePlayerHover(-1);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        completeLiftAnimation(state);
    }

    @Test
    void testHandlePlayerHoverWithSelectedIndex() {
        visualizer.setPlayerSelectedIndex(0);
        completeLiftAnimation(visualizer.animationStates.get(ArrayOwner.PLAYER));
        visualizer.handlePlayerHover(1);
        assertEquals(1, visualizer.playerHoveredIndex, "Hovered index should be 1");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertEquals(1, state.liftedIndex, "Lift animation should start for hovered index 1");
        assertTrue(state.lifting, "Lift animation should be upward");

        visualizer.handlePlayerHover(0);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        completeLiftAnimation(state);

        visualizer.handlePlayerHover(-1);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        completeLiftAnimation(state);
    }

    @Test
    void testHandlePlayerHoverGameInactive() {
        when(gameLogic.isGameActive()).thenReturn(false);
        visualizer.handlePlayerHover(1);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertNull(state.liftedIndex, "No lift animation when game inactive");
    }

    @Test
    void testHandlePlayerHoverPaused() {
        when(gameLogic.isPaused()).thenReturn(true);
        visualizer.handlePlayerHover(1);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertNull(state.liftedIndex, "No lift animation when paused");
    }

    @Test
    void testHandlePlayerHoverAnimationInProgress() {
        when(gameLogic.isAnimationInProgress(ArrayOwner.PLAYER)).thenReturn(true);
        visualizer.handlePlayerHover(1);
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertNull(state.liftedIndex, "No lift animation when animation in progress");
    }

    @Test
    void testSetPlayerSelectedIndex() {
        visualizer.setPlayerSelectedIndex(1);
        assertEquals(1, visualizer.playerSelectedIndex, "Selected index should be 1");
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertEquals(1, state.liftedIndex, "Lift animation should start for index 1");
        assertTrue(state.lifting, "Lift animation should be upward");

        visualizer.setPlayerSelectedIndex(null);
        assertNull(visualizer.playerSelectedIndex, "Selected index should be null");
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be null");
        completeLiftAnimation(state);
    }

    @Test
    void testStartPlayerSwapAnimation() {
        visualizer.startPlayerSwapAnimation(0, 1);
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertArrayEquals(new int[]{0, 1}, state.swappingIndices, "Swapping indices should be set");
        assertEquals(0f, state.progress, "Animation progress should start at 0");
        assertNotNull(state.numbersBeingSwapped, "Numbers being swapped should be set");
        assertArrayEquals(new int[]{1, 2, 3, 4}, state.numbersBeingSwapped[0], "First number should be copied");
        assertArrayEquals(new int[]{5, 6, 7, 8}, state.numbersBeingSwapped[1], "Second number should be copied");
        assertTrue(state.timer.isRunning(), "Timer should be running");
    }

    @Test
    void testGetCubeIndexAt() {
        int x = visualizer.getCubeSpacing() + visualizer.getCubeWidth() / 2;
        int y = visualizer.getRowPadding() + visualizer.getLabelOffset() + 5 + visualizer.getCubeHeight() / 2;
        assertEquals(0, visualizer.getCubeIndexAt(x, y, ArrayOwner.PLAYER), "Should detect cube 0");

        x = 0;
        y = 0;
        assertEquals(-1, visualizer.getCubeIndexAt(x, y, ArrayOwner.PLAYER), "Should return -1 for invalid coords");

        x = visualizer.getCubeSpacing() + visualizer.getCubeWidth() + visualizer.getCubeSpacing() / 2;
        y = visualizer.getRowPadding() + visualizer.getLabelOffset() + 5 + visualizer.getCubeHeight() / 2;
        assertEquals(-1, visualizer.getCubeIndexAt(x, y, ArrayOwner.PLAYER), "Should return -1 for spacing");
    }

    @Test
    void testUpdateLocale() {
        ResourceBundle newMessages = mock(ResourceBundle.class);
        when(newMessages.getString(anyString())).thenReturn("Mocked");
        visualizer.messages = newMessages;
        visualizer.updateLocale();
        assertNotNull(visualizer.messages, "Messages should be updated");
    }

    @Test
    void testGetStrategyName() {
        assertEquals("Bubbler", visualizer.getStrategyName("bubbleSort"), "Should return localized strategy name");
        verify(messages).getString("strategy.bubbleSort");
    }

    @Test
    void testOnSwapNonPlayer() {
        visualizer.onSwap(0, 1, true, ArrayOwner.FAST_AI);
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.FAST_AI);
        assertArrayEquals(new int[]{0, 1}, state.swappingIndices, "Swap animation should start for Fast AI");
        verify(gameLogic).setAnimationInProgress(true, ArrayOwner.FAST_AI);

        visualizer.onSwap(0, 1, false, ArrayOwner.FAST_AI);
        assertArrayEquals(new int[]{0, 1}, state.swappingIndices, "No new animation for failed swap");
    }

    @Test
    void testOnSwapPlayer() {
        visualizer.onSwap(0, 1, true, ArrayOwner.PLAYER);
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        assertNull(state.swappingIndices, "No swap animation for player (handled by controller)");
    }

    @Test
    void testOnDigitRevealed() {
        visualizer.onDigitRevealed(2, ArrayOwner.PLAYER);
        assertTrue(true, "onDigitRevealed should trigger repaint without errors");
    }

    @Test
    void testOnFinalStageStarted() {
        visualizer.onFinalStageStarted(ArrayOwner.FAST_AI);
        assertTrue(true, "onFinalStageStarted should trigger repaint without errors");
    }

    @Test
    void testOnWin() {
        visualizer.playerSelectedIndex = 1;
        visualizer.playerHoveredIndex = 2;
        visualizer.onWin();
        assertNull(visualizer.playerSelectedIndex, "Selected index should be reset");
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be reset");
        for (BlindSortVisualizer.AnimationState state : visualizer.animationStates.values()) {
            assertNull(state.swappingIndices, "Swap animation should be stopped");
            assertNull(state.liftedIndex, "Lift animation should be stopped");
            assertFalse(state.timer.isRunning(), "Timer should be stopped");
        }
    }

    @Test
    void testOnLose() {
        visualizer.playerSelectedIndex = 1;
        visualizer.playerHoveredIndex = 2;
        visualizer.onLose();
        assertNull(visualizer.playerSelectedIndex, "Selected index should be reset");
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be reset");
        for (BlindSortVisualizer.AnimationState state : visualizer.animationStates.values()) {
            assertNull(state.swappingIndices, "Swap animation should be stopped");
            assertNull(state.liftedIndex, "Lift animation should be stopped");
            assertFalse(state.timer.isRunning(), "Timer should be stopped");
        }
    }

    @Test
    void testResetAnimation() {
        visualizer.playerSelectedIndex = 1;
        visualizer.playerHoveredIndex = 2;
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        state.startSwapAnimation(0, 1);
        visualizer.resetAnimation();
        assertNull(visualizer.playerSelectedIndex, "Selected index should be reset");
        assertNull(visualizer.playerHoveredIndex, "Hovered index should be reset");
        assertNull(state.swappingIndices, "Swap animation should be stopped");
        assertNull(state.liftedIndex, "Lift animation should be stopped");
        assertFalse(state.timer.isRunning(), "Timer should be stopped");
    }

    @Test
    void testZeroNumbersCount() {
        BlindSortLogic emptyLogic = mock(BlindSortLogic.class);
        when(emptyLogic.getNumbersCopy()).thenReturn(new int[][]{{1, 2, 3, 4}});
        when(emptyLogic.getNumbersCopy(any())).thenReturn(new int[][]{{1, 2, 3, 4}});
        when(emptyLogic.getVisibleDigitsForOwner(any())).thenReturn(1);
        when(emptyLogic.isFinalStage(any())).thenReturn(false);
        when(emptyLogic.isGameActive()).thenReturn(true);
        when(emptyLogic.isPaused()).thenReturn(false);
        when(emptyLogic.getFastAIStrategyName()).thenReturn("bubbleSort");
        when(emptyLogic.getSlowAIStrategyName()).thenReturn("insertionSort");

        BlindSortVisualizer emptyVisualizer = new BlindSortVisualizer(emptyLogic, 800, 600);
        emptyVisualizer.messages = messages;
        assertTrue(true, "Should handle visualization without errors");

        when(emptyLogic.getNumbersCopy()).thenReturn(new int[0][]);
        when(emptyLogic.getNumbersCopy(any())).thenReturn(new int[0][]);
        assertThrows(ArithmeticException.class, () -> new BlindSortVisualizer(emptyLogic, 800, 600),
                "Should throw ArithmeticException for zero numbers");
    }

    @Test
    void testAnimationStateTimer() {
        BlindSortVisualizer.AnimationState state = visualizer.animationStates.get(ArrayOwner.PLAYER);
        state.startSwapAnimation(0, 1);
        assertTrue(state.timer.isRunning(), "Timer should be running");

        ActionListener listener = state.timer.getActionListeners()[0];
        state.progress = 0.95f;
        listener.actionPerformed(null);
        assertEquals(1f, state.progress, "Progress should reach 1");
        assertNull(state.swappingIndices, "Swapping indices should be reset");
        assertFalse(state.timer.isRunning(), "Timer should be stopped");
        verify(controllerCallback).handlePlayerSwapAnimationFinished();
    }
}