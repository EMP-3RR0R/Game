package com.wormfarm.minigames.blindsort.ai.strategy;

import java.util.Optional;
import java.util.Random;

public class SelectionSortStrategy implements AISortingStrategy {
    private final Random random = new Random();

    @Override
    public Optional<int[]> getNextMove(int[][] numbers, int visibleDigits, double errorChance) {
        int n = numbers.length;

        if (errorChance > 0 && random.nextDouble() < errorChance) {
            int idx1 = random.nextInt(n);
            int idx2 = random.nextInt(n);
            if (idx1 == idx2) {
                idx2 = (idx1 + 1) % n;
            }
            return Optional.of(new int[]{idx1, idx2});
        }

        for (int i = 0; i < n; i++) {
            int bestIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (compareNumbers(numbers[j], numbers[bestIndex], visibleDigits) < 0) {
                    bestIndex = j;
                }
            }
            if (bestIndex != i) {
                return Optional.of(new int[]{i, bestIndex});
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "selection";
    }

    private int compareNumbers(int[] num1Digits, int[] num2Digits, int visibleDigits) {
        if (visibleDigits == 5) {
            long num1 = convertDigitsToLong(num1Digits);
            long num2 = convertDigitsToLong(num2Digits);
            return Long.compare(num1, num2);
        } else {
            int digitIndex = visibleDigits - 1;
            return Integer.compare(num1Digits[digitIndex], num2Digits[digitIndex]);
        }
    }

    private long convertDigitsToLong(int[] digits) {
        long number = 0;
        for (int digit : digits) {
            number = number * 10 + digit;
        }
        return number;
    }
}