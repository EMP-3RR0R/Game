package com.wormfarm.minigames.blindsort.logic;

import com.wormfarm.minigames.blindsort.ai.FastBlindSortAI;
import com.wormfarm.minigames.blindsort.ai.SlowBlindSortAI;
import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlindSortLogicTest {
    private BlindSortLogic logic;
    private BlindSortEventListener listener;
    private FastBlindSortAI fastAI;
    private SlowBlindSortAI slowAI;

    @BeforeEach
    void setUp() {
        logic = new BlindSortLogic(3);

        listener = mock(BlindSortEventListener.class);
        logic.addEventListener(listener);

        fastAI = mock(FastBlindSortAI.class);
        slowAI = mock(SlowBlindSortAI.class);

        logic.fastAI = fastAI;
        logic.slowAI = slowAI;
    }

    private void setNumbers(ArrayOwner owner, int[][] numbers) {
        int[][] target = owner == ArrayOwner.PLAYER ? logic.playerNumbers :
                owner == ArrayOwner.FAST_AI ? logic.fastAINumbers : logic.slowAINumbers;
        for (int i = 0; i < numbers.length && i < target.length; i++) {
            System.arraycopy(numbers[i], 0, target[i], 0, 4);
        }
    }

    private void setVisibleDigits(ArrayOwner owner, int digits) {
        logic.ownerVisibleDigits.put(owner, digits);
    }

    private void setFinalStage(ArrayOwner owner, boolean value) {
        logic.ownerFinalStageActive.put(owner, value);
        if (value) {
            setVisibleDigits(owner, 5);
        }
    }

    @Test
    void testInitialization() {
        assertTrue(logic.isGameActive, "Game should be active");
        assertEquals(0, logic.getSwapCount(), "Swap count should be 0");
        assertEquals(1, logic.getVisibleDigits(), "Player should see 1 digit");
        assertEquals(1, logic.getVisibleDigitsForOwner(ArrayOwner.FAST_AI), "Fast AI should see 1 digit");
        assertEquals(1, logic.getVisibleDigitsForOwner(ArrayOwner.SLOW_AI), "Slow AI should see 1 digit");
        assertFalse(logic.isFinalStage(), "Player should not be in final stage");
        assertFalse(logic.isPaused(), "Game should not be paused");
    }

    @Test
    void testSwapNumbersValid() {
        int[][] numbers = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);

        boolean result = logic.swapNumbers(0, 1);
        assertTrue(result, "Swap should succeed");
        assertEquals(1, logic.getSwapCount(), "Swap count should increment");

        int[][] expected = {{5, 6, 7, 8}, {1, 2, 3, 4}, {9, 0, 1, 2}};
        assertArrayEquals(expected, logic.getNumbersCopy(ArrayOwner.PLAYER), "Numbers should be swapped");

        verify(listener).onSwap(0, 1, true, ArrayOwner.PLAYER);
    }

    @Test
    void testSwapNumbersInvalidIndices() {
        assertFalse(logic.swapNumbers(-1, 0), "Swap with negative index should fail");
        assertFalse(logic.swapNumbers(0, 3), "Swap with out-of-bounds index should fail");
        assertFalse(logic.swapNumbers(0, 0), "Swap with same index should fail");

        verify(listener).onSwap(-1, 0, false, ArrayOwner.PLAYER);
        verify(listener).onSwap(0, 3, false, ArrayOwner.PLAYER);
        verify(listener).onSwap(0, 0, false, ArrayOwner.PLAYER);
        assertEquals(0, logic.getSwapCount(), "Swap count should not increment");
    }

    @Test
    void testSwapNumbersDuringAnimation() {
        logic.animationInProgressMap.get(ArrayOwner.PLAYER).set(true);
        assertFalse(logic.swapNumbers(0, 1), "Swap during animation should fail");
        verify(listener).onSwap(0, 1, false, ArrayOwner.PLAYER);
        assertEquals(0, logic.getSwapCount(), "Swap count should not increment");
    }

    @Test
    void testIsSortedSingleDigit() {
        int[][] numbers = {{1, 2, 3, 4}, {2, 6, 7, 8}, {3, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);
        setVisibleDigits(ArrayOwner.PLAYER, 1);

        assertTrue(logic.isSorted(ArrayOwner.PLAYER), "Array should be sorted by first digit");

        numbers[1][0] = 0;
        setNumbers(ArrayOwner.PLAYER, numbers);
        assertFalse(logic.isSorted(ArrayOwner.PLAYER), "Array should not be sorted");
    }

    @Test
    void testIsSortedFinalStage() {
        int[][] numbers = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);
        setFinalStage(ArrayOwner.PLAYER, true);

        assertTrue(logic.isSorted(ArrayOwner.PLAYER), "Array should be sorted in final stage");

        numbers[1] = new int[]{9, 9, 9, 9};
        setNumbers(ArrayOwner.PLAYER, numbers);
        assertFalse(logic.isSorted(ArrayOwner.PLAYER), "Array should not be sorted");
    }

    @Test
    void testAdvanceStage() {
        int[][] numbers = {{1, 2, 3, 4}, {2, 6, 7, 8}, {3, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);
        setVisibleDigits(ArrayOwner.PLAYER, 1);

        logic.checkAndAdvanceStageForOwner(ArrayOwner.PLAYER);
        assertEquals(2, logic.getVisibleDigitsForOwner(ArrayOwner.PLAYER), "Should advance to 2 digits");
        verify(listener).onDigitRevealed(2, ArrayOwner.PLAYER);
    }

    @Test
    void testFinalStage() {
        setVisibleDigits(ArrayOwner.PLAYER, 4);
        int[][] numbers = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);

        logic.checkAndAdvanceStageForOwner(ArrayOwner.PLAYER);
        assertFalse(logic.isFinalStage(ArrayOwner.PLAYER), "Should enter final stage");

        int[][] remixed = logic.getNumbersCopy(ArrayOwner.PLAYER);
        assertEquals(Arrays.deepToString(numbers), Arrays.deepToString(remixed), "Numbers should be remixed");
    }

    @Test
    void testWinCondition() {
        int[][] numbers = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);
        setFinalStage(ArrayOwner.PLAYER, true);

        logic.checkAndAdvanceStageForOwner(ArrayOwner.PLAYER);
        assertFalse(logic.isGameActive, "Game should end");
        verify(listener).onWin();
        verify(fastAI).stopSorting();
        verify(slowAI).stopSorting();
    }

    @Test
    void testLoseCondition() {
        int[][] numbers = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.FAST_AI, numbers);
        setFinalStage(ArrayOwner.FAST_AI, true);

        logic.checkAndAdvanceStageForOwner(ArrayOwner.FAST_AI);
        assertFalse(logic.isGameActive, "Game should end");
        verify(listener).onLose();
        verify(fastAI).stopSorting();
        verify(slowAI).stopSorting();
    }

    @Test
    void testPauseAndResume() {
        logic.pauseGame();
        assertTrue(logic.isPaused(), "Game should be paused");
        verify(fastAI).stopSorting();
        verify(slowAI).stopSorting();

        long elapsedBefore = logic.getElapsedTime();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Игнорируем
        }

        logic.resumeGame();
        assertFalse(logic.isPaused(), "Game should be resumed");
        long elapsedAfter = logic.getElapsedTime();
        assertTrue(elapsedAfter >= elapsedBefore, "Elapsed time should not increase significantly during pause");
        verify(fastAI).startSorting(logic);
        verify(slowAI).startSorting(logic);
    }

    @Test
    void testResetGame() {
        logic.swapNumbers(0, 1);
        logic.pauseGame();

        logic.resetGame();
        assertTrue(logic.isGameActive, "Game should be active");
        assertEquals(0, logic.getSwapCount(), "Swap count should be reset");
        assertEquals(1, logic.getVisibleDigits(), "Visible digits should be reset");
        assertFalse(logic.isFinalStage(), "Final stage should be reset");
        assertTrue(logic.isPaused(), "Game should not be paused");
    }

    @Test
    void testNumbersCopyImmutability() {
        int[][] copy = logic.getNumbersCopy(ArrayOwner.PLAYER);
        copy[0][0] = 999;
        assertNotEquals(999, logic.getNumbersCopy(ArrayOwner.PLAYER)[0][0], "Original numbers should not be modified");
    }

    @Test
    void testInvalidDigitInConvertDigitsToLong() {
        int[][] numbers = {{10, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}};
        setNumbers(ArrayOwner.PLAYER, numbers);
        setFinalStage(ArrayOwner.PLAYER, true);

        assertThrows(IllegalArgumentException.class, () -> logic.isSorted(ArrayOwner.PLAYER),
                "Invalid digit should throw exception");
    }

    @Test
    void testZeroNumbersCount() {
        BlindSortLogic emptyLogic = new BlindSortLogic(0);
        assertEquals(0, emptyLogic.getNumbersCopy().length, "Should handle zero numbers");
        assertFalse(emptyLogic.swapNumbers(0, 1), "Swap should fail with zero numbers");
        assertTrue(emptyLogic.isSorted(), "Empty array should be sorted");
    }
}