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

class MergeSortStrategyTest {

    @InjectMocks
    private MergeSortStrategy mergeSortStrategy;

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

        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 5, 0.0);
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

        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 4, 0.0);
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

        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 5, 0.0);
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

        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 4, 0.0);
        assertFalse(nextMove.isPresent());
    }

    @Test
    void shouldReturnEmptyOptionalForEmptyArray() {
        int[][] numbers = {};
        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 5, 0.0);
        assertFalse(nextMove.isPresent());
        verify(mockRandom, never()).nextDouble();
    }

    @Test
    void shouldReturnEmptyOptionalForArrayWithOneElement() {
        int[][] numbers = {{1, 2, 3, 4, 5}};
        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 5, 0.0);
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

        Optional<int[]> nextMove = mergeSortStrategy.getNextMove(numbers, 5, 0.0);
        assertTrue(nextMove.isPresent());
        assertArrayEquals(new int[]{0, 1}, nextMove.get());
    }

    private final int[][] sortedNumbersForErrorChance = {
            {1, 1, 1, 1, 1},
            {2, 2, 2, 2, 2},
            {3, 3, 3, 3, 3}
    };

    @Test
    void compareNumbersShouldCompareFullNumbersWhenVisibleDigitsIs5Num1GreaterThanNum2() {
        int[] num1 = {1, 2, 3, 4, 7};
        int[] num2 = {1, 2, 3, 4, 6};
        assertTrue(mergeSortStrategy.compareNumbers(num1, num2, 5) > 0);
    }

    @Test
    void compareNumbersShouldCompareFullNumbersWhenVisibleDigitsIs5Num1EqualsNum2() {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 2, 3, 4, 5};
        assertEquals(0, mergeSortStrategy.compareNumbers(num1, num2, 5));
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1LessThanNum2() {
        int[] num1 = {1, 2, 3, 4, 5};
        int[] num2 = {1, 2, 4, 0, 0};
        assertTrue(mergeSortStrategy.compareNumbers(num1, num2, 3) < 0);
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1GreaterThanNum2() {
        int[] num1 = {1, 2, 5, 4, 5};
        int[] num2 = {1, 2, 4, 0, 0};
        assertTrue(mergeSortStrategy.compareNumbers(num1, num2, 3) > 0);
    }

    @Test
    void compareNumbersShouldCompareByLastVisibleDigitWhenVisibleDigitsIsLessThan5Num1EqualsNum2() {
        int[] num1 = {1, 2, 3, 4, 5}; // Last visible digit: 3
        int[] num2 = {1, 0, 3, 9, 9}; // Last visible digit: 3
        assertEquals(0, mergeSortStrategy.compareNumbers(num1, num2, 3));
    }

    @Test
    void compareNumbersShouldHandleLeadingZerosCorrectlyInFullNumberComparison() {
        int[] num1 = {0, 0, 1, 2, 3};
        int[] num2 = {0, 0, 0, 9, 9};
        assertTrue(mergeSortStrategy.compareNumbers(num1, num2, 5) > 0);
    }

    @Test
    void compareNumbersShouldHandleLeadingZerosCorrectlyInPartialComparison() {
        int[] num1 = {0, 0, 1, 2, 3};
        int[] num2 = {0, 0, 0, 9, 9};
        assertTrue(mergeSortStrategy.compareNumbers(num1, num2, 3) > 0);
    }

    @Test
    void convertDigitsToLongShouldConvertSingleDigitArrayToLong() {
        assertEquals(5L, mergeSortStrategy.convertDigitsToLong(new int[]{5}));
    }

    @Test
    void convertDigitsToLongShouldConvertMultipleDigitArrayToLong() {
        assertEquals(12345L, mergeSortStrategy.convertDigitsToLong(new int[]{1, 2, 3, 4, 5}));
    }

    @Test
    void convertDigitsToLongShouldConvertArrayWithLeadingZerosToCorrectLongValue() {
        assertEquals(123L, mergeSortStrategy.convertDigitsToLong(new int[]{0, 0, 1, 2, 3}));
    }

    @Test
    void convertDigitsToLongShouldConvertArrayOfAllZerosToZeroLongValue() {
        assertEquals(0L, mergeSortStrategy.convertDigitsToLong(new int[]{0, 0, 0, 0, 0}));
    }

    @Test
    void convertDigitsToLongShouldConvertEmptyArrayToZeroLongValue() {
        assertEquals(0L, mergeSortStrategy.convertDigitsToLong(new int[]{}));
    }

    @Test
    void getNameMethodShouldReturnMerge() {
        assertEquals("merge", mergeSortStrategy.getName());
    }
}