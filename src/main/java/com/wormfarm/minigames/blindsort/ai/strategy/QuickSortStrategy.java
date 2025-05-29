package com.wormfarm.minigames.blindsort.ai.strategy;

import java.util.Optional;
import java.util.Random;

public class QuickSortStrategy implements AISortingStrategy {
    private final Random random = new Random();

    @Override
    public Optional<int[]> getNextMove(int[][] numbers, int visibleDigits, double errorChance) {
        int n = numbers.length;

        // Проверка на ошибку
        if (errorChance > 0 && random.nextDouble() < errorChance) {
            int idx1 = random.nextInt(n);
            int idx2 = random.nextInt(n);
            if (idx1 == idx2) {
                idx2 = (idx1 + 1) % n;
            }
            return Optional.of(new int[]{idx1, idx2});
        }

        // Быстрая сортировка: выбираем опорный элемент и ищем перестановку
        int pivotIndex = n - 1; // Опорный элемент — последний
        for (int i = 0; i < pivotIndex; i++) {
            if (compareNumbers(numbers[i], numbers[pivotIndex], visibleDigits) > 0) {
                return Optional.of(new int[]{i, pivotIndex});
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "quick";
    }

    int compareNumbers(int[] num1Digits, int[] num2Digits, int visibleDigits) {
        if (visibleDigits == 5) {
            long num1 = convertDigitsToLong(num1Digits);
            long num2 = convertDigitsToLong(num2Digits);
            return Long.compare(num1, num2);
        } else {
            int digitIndex = visibleDigits - 1;
            return Integer.compare(num1Digits[digitIndex], num2Digits[digitIndex]);
        }
    }

    long convertDigitsToLong(int[] digits) {
        long number = 0;
        for (int digit : digits) {
            number = number * 10 + digit;
        }
        return number;
    }
}