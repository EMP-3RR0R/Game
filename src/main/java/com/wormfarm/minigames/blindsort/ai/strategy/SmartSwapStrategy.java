package com.wormfarm.minigames.blindsort.ai.strategy;

import java.util.Optional;
import java.util.Random;

public class SmartSwapStrategy implements AISortingStrategy {
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
            int targetIndex = -1;
            long bestCompareValue = (visibleDigits == 5) ? convertDigitsToLong(numbers[i]) : numbers[i][visibleDigits - 1];

            for (int j = i + 1; j < n; j++) {
                long currentCompareValue = (visibleDigits == 5) ? convertDigitsToLong(numbers[j]) : numbers[j][visibleDigits - 1];
                if (currentCompareValue < bestCompareValue) {
                    bestCompareValue = currentCompareValue;
                    targetIndex = j;
                }
            }

            if (targetIndex != -1) {
                return Optional.of(new int[]{i, targetIndex});
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "smart";
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