package com.wormfarm.minigames.blindsort.ai.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BubbleSortStrategyTest {

    @InjectMocks
    private BubbleSortStrategy bubbleSortStrategy;

    @Mock
    private Random mockRandom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnFirstUnsortedPairWhenArrayIsUnsortedFullVisibility() {
        int[][] numbers = {
                {1, 2, 3, 4, 5},
                {1, 2, 3, 4, 0},
                {1, 2, 3, 4, 6}
        };
        when(mockRandom.nextDouble()).thenReturn(0.9);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertTrue(nextMove.isPresent());
        assertArrayEquals(new int[]{0, 1}, nextMove.get());
    }

    @Test
    void shouldReturnFirstUnsortedPairWhenArrayIsUnsortedPartialVisibility() {
        int[][] numbers = {
                {1, 2, 3, 4, 5},
                {1, 2, 3, 3, 9},
                {1, 2, 3, 5, 0}
        };
        when(mockRandom.nextDouble()).thenReturn(0.9);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 4, 0.0);
        assertTrue(nextMove.isPresent());
        assertArrayEquals(new int[]{0, 1}, nextMove.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenArrayIsAlreadySortedFullVisibility() {
        int[][] numbers = {
                {1, 1, 1, 1, 1},
                {2, 2, 2, 2, 2},
                {3, 3, 3, 3, 3}
        };
        when(mockRandom.nextDouble()).thenReturn(0.9);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertFalse(nextMove.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalWhenArrayIsAlreadySortedPartialVisibility() {
        int[][] numbers = {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 2},
                {1, 1, 1, 1, 3}
        };
        when(mockRandom.nextDouble()).thenReturn(0.9);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertFalse(nextMove.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalForEmptyArray() {
        int[][] numbers = {};
        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertFalse(nextMove.isPresent());
        verify(mockRandom, never()).nextDouble();
    }

    @Test
    void shouldReturnEmptyOptionalForArrayWithOneElement() {
        int[][] numbers = {{1, 2, 3, 4, 5}};
        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertFalse(nextMove.isPresent());
        verify(mockRandom, never()).nextDouble();
    }

    @Test
    void shouldHandleNumbersWithLeadingZerosCorrectlyFullVisibility() {
        int[][] numbers = {
                {0, 0, 0, 1, 0},
                {0, 0, 0, 0, 9}
        };
        when(mockRandom.nextDouble()).thenReturn(0.9);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(numbers, 5, 0.0);
        assertTrue(nextMove.isPresent());
        assertArrayEquals(new int[]{0, 1}, nextMove.get());
    }

    private final int[][] errorChanceNumbers = {
            {1, 1, 1, 1, 1},
            {2, 2, 2, 2, 2},
            {3, 3, 3, 3, 3}
    };

    @Test
    void shouldReturnRandomIndicesWhenErrorChanceIsMet() {
        when(mockRandom.nextDouble()).thenReturn(0.05);
        when(mockRandom.nextInt(errorChanceNumbers.length)).thenReturn(0, 2);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(errorChanceNumbers, 5, 0.1);
        assertFalse(nextMove.isPresent());
    }

    @Test
    void shouldReturnRandomIndicesWhenErrorChanceIsMetAndIndicesAreInitiallySame() {
        when(mockRandom.nextDouble()).thenReturn(0.05);
        when(mockRandom.nextInt(errorChanceNumbers.length)).thenReturn(1, 1, 2);

        Optional<int[]> nextMove = bubbleSortStrategy.getNextMove(errorChanceNumbers, 5, 0.1);
        assertFalse(nextMove.isPresent());
    }

    private int callCompareNumbers(int[] num1Digits, int[] num2Digits, int visibleDigits) throws Exception {
        java.lang.reflect.Method method = BubbleSortStrategy.class.getDeclaredMethod("compareNumbers", int[].class, int[].class, int.class);
        method.setAccessible(true);
        return (int) method.invoke(bubbleSortStrategy, num1Digits, num2Digits, visibleDigits);
    }

    @Test
    void compareNumbersShouldCompareFullNumbersWhenVisibleDigitsIs5Num1LessThanNum2() throws Exception {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 2, 3, 4, 6};
        assertTrue(callCompareNumbers(num1, num2, 5) < 0);
    }

    @Test
    void compareNumbersShouldCompareFullNumbersWhenVisibleDigitsIs5Num1GreaterThanNum2() throws Exception {
        int[] num1 = {1, 2, 3, 4, 7};
        int[] num2 = {1, 2, 3, 4, 6};
        assertTrue(callCompareNumbers(num1, num2, 5) > 0);
    }

    @Test
    void compareNumbersShouldCompareFullNumbersWhenVisibleDigitsIs5Num1EqualsNum2() throws Exception {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 2, 3, 4, 5};
        assertEquals(0, callCompareNumbers(num1, num2, 5));
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1LessThanNum2() throws Exception {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 2, 4, 0, 0};
        assertTrue(callCompareNumbers(num1, num2, 3) < 0);
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1GreaterThanNum2() throws Exception {
        int[] num1 = {1, 2, 5, 4, 5};
        int[] num2 = {1, 2, 4, 0, 0};
        assertTrue(callCompareNumbers(num1, num2, 3) > 0);
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1EqualsNum2() throws Exception {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 0, 3, 9, 9};
        assertEquals(0, callCompareNumbers(num1, num2, 3));
    }

    @Test
    void compareNumbersShouldHandleLeadingZerosCorrectlyInFullNumberComparison() throws Exception {
        int[] num1 = {0, 0, 1, 2, 3};
        int[] num2 = {0, 0, 0, 9, 9};
        assertTrue(callCompareNumbers(num1, num2, 5) > 0);
    }

    @Test
    void compareNumbersShouldHandleLeadingZerosCorrectlyInPartialComparison() throws Exception {
        int[] num1 = {0, 0, 1, 2, 3};
        int[] num2 = {0, 0, 0, 9, 9};
        assertTrue(callCompareNumbers(num1, num2, 3) > 0);
    }

    private long callConvertDigitsToLong(int[] digits) throws Exception {
        java.lang.reflect.Method method = BubbleSortStrategy.class.getDeclaredMethod("convertDigitsToLong", int[].class);
        method.setAccessible(true);
        return (long) method.invoke(bubbleSortStrategy, digits);
    }

    @Test
    void convertDigitsToLongShouldConvertSingleDigitArrayToLong() throws Exception {
        assertEquals(5L, callConvertDigitsToLong(new int[]{5}));
    }

    @Test
    void convertDigitsToLongShouldConvertMultipleDigitArrayToLong() throws Exception {
        assertEquals(12345L, callConvertDigitsToLong(new int[]{1, 2, 3, 4, 5}));
    }

    @Test
    void convertDigitsToLongShouldConvertArrayWithLeadingZerosToCorrectLongValue() throws Exception {
        assertEquals(123L, callConvertDigitsToLong(new int[]{0, 0, 1, 2, 3}));
    }

    @Test
    void convertDigitsToLongShouldConvertArrayOfAllZerosToZeroLongValue() throws Exception {
        assertEquals(0L, callConvertDigitsToLong(new int[]{0, 0, 0, 0, 0}));
    }

    @Test
    void convertDigitsToLongShouldConvertEmptyArrayToZeroLongValue() throws Exception {
        assertEquals(0L, callConvertDigitsToLong(new int[]{}));
    }

    @Test
    void getNameMethodShouldReturnBubble() {
        assertEquals("bubble", bubbleSortStrategy.getName());
    }
}