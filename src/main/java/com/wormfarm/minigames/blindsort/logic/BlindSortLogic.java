package com.wormfarm.minigames.blindsort.logic;

import com.wormfarm.minigames.blindsort.ai.BlindSortAI;
import com.wormfarm.minigames.blindsort.ai.FastBlindSortAI;
import com.wormfarm.minigames.blindsort.ai.SlowBlindSortAI;
import com.wormfarm.minigames.blindsort.api.BlindSortGame;
import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.ai.strategy.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlindSortLogic implements BlindSortGame {
    public enum ArrayOwner {
        PLAYER, FAST_AI, SLOW_AI
    }

    public final int[][] playerNumbers;
    final int[][] fastAINumbers;
    final int[][] slowAINumbers;

    final Map<ArrayOwner, Integer> ownerVisibleDigits = new EnumMap<>(ArrayOwner.class);
    final Map<ArrayOwner, Boolean> ownerFinalStageActive = new EnumMap<>(ArrayOwner.class);

    private final List<BlindSortEventListener> listeners = new ArrayList<>();
    private final Random random = new Random();
    private transient long startTime;
    BlindSortAI fastAI;
    BlindSortAI slowAI;
    private boolean paused;
    private long pauseTime;

    private int swapCount = 0;
    public boolean isGameActive;

    final Map<ArrayOwner, AtomicBoolean> animationInProgressMap = new EnumMap<>(ArrayOwner.class);
    private String fastAIStrategyName;
    private String slowAIStrategyName;

    public BlindSortLogic(int numbersCount) {
        this.playerNumbers = new int[numbersCount][4];
        this.fastAINumbers = new int[numbersCount][4];
        this.slowAINumbers = new int[numbersCount][4];

        for (ArrayOwner owner : ArrayOwner.values()) {
            animationInProgressMap.put(owner, new AtomicBoolean(false));
            ownerVisibleDigits.put(owner, 1);
            ownerFinalStageActive.put(owner, false);
        }

        createAI();
        resetGame();
    }

    private void createAI() {
        if (fastAI != null) fastAI.stopSorting();
        if (slowAI != null) slowAI.stopSorting();

        List<AISortingStrategy> fastStrategies = Arrays.asList(
                new BubbleSortStrategy(),
                new MergeSortStrategy(),
                new QuickSortStrategy()
        );
        List<AISortingStrategy> slowStrategies = Arrays.asList(
                new InsertionSortStrategy(),
                new SelectionSortStrategy(),
                new SmartSwapStrategy()
        );

        AISortingStrategy fastStrategy = fastStrategies.get(random.nextInt(fastStrategies.size()));
        AISortingStrategy slowStrategy = slowStrategies.get(random.nextInt(slowStrategies.size()));

        this.fastAI = new FastBlindSortAI(ArrayOwner.FAST_AI);
        ((FastBlindSortAI) this.fastAI).setStrategy(fastStrategy, 0.20);
        this.fastAIStrategyName = fastStrategy.getName();

        this.slowAI = new SlowBlindSortAI(ArrayOwner.SLOW_AI);
        ((SlowBlindSortAI) this.slowAI).setStrategy(slowStrategy, 0.05);
        this.slowAIStrategyName = slowStrategy.getName();
    }

    public String getFastAIStrategyName() {
        return fastAIStrategyName;
    }

    public String getSlowAIStrategyName() {
        return slowAIStrategyName;
    }

    @Override
    public int[][] getNumbersCopy() {
        return getNumbersCopy(ArrayOwner.PLAYER);
    }

    public int[][] getNumbersCopy(ArrayOwner owner) {
        int[][] sourceNumbers = getNumbers(owner);
        int[][] copy = new int[sourceNumbers.length][];
        for (int i = 0; i < sourceNumbers.length; i++) {
            copy[i] = Arrays.copyOf(sourceNumbers[i], sourceNumbers[i].length);
        }
        return copy;
    }

    @Override
    public int getVisibleDigits() {
        return getVisibleDigitsForOwner(ArrayOwner.PLAYER);
    }

    @Override
    public int getVisibleDigitsForOwner(ArrayOwner owner) {
        return ownerVisibleDigits.getOrDefault(owner, 1);
    }

    @Override
    public boolean isFinalStage(ArrayOwner owner) {
        return ownerFinalStageActive.getOrDefault(owner, false);
    }

    @Override
    public boolean swapNumbers(int index1, int index2) {
        if (!isGameActive || isAnimationInProgress(ArrayOwner.PLAYER)) {
            fireSwapEvent(index1, index2, false, ArrayOwner.PLAYER);
            return false;
        }

        if (index1 < 0 || index2 < 0 || index1 >= playerNumbers.length || index2 >= playerNumbers.length || index1 == index2) {
            fireSwapEvent(index1, index2, false, ArrayOwner.PLAYER);
            return false;
        }

        if (isFinalStage(ArrayOwner.PLAYER) && isSorted(ArrayOwner.PLAYER)) {
            fireSwapEvent(index1, index2, false, ArrayOwner.PLAYER);
            return false;
        }

        boolean swapped = swapNumbersInternal(index1, index2, ArrayOwner.PLAYER);
        if (swapped) {
            swapCount++;
        }
        return swapped;
    }

    public boolean swapNumbersInternal(int index1, int index2, ArrayOwner owner) {
        if (!isGameActive || index1 < 0 || index2 < 0 ||
                index1 >= getNumbers(owner).length || index2 >= getNumbers(owner).length || index1 == index2) {
            return false;
        }
        if (isFinalStage(owner) && isSorted(owner)) {
            fireSwapEvent(index1, index2, false, owner);
            return false;
        }

        int[] temp = getNumbers(owner)[index1];
        getNumbers(owner)[index1] = getNumbers(owner)[index2];
        getNumbers(owner)[index2] = temp;

        fireSwapEvent(index1, index2, true, owner);
        checkAndAdvanceStageForOwner(owner);
        return true;
    }

    private int[][] getNumbers(ArrayOwner owner) {
        switch (owner) {
            case PLAYER:
                return playerNumbers;
            case FAST_AI:
                return fastAINumbers;
            case SLOW_AI:
                return slowAINumbers;
            default:
                throw new IllegalArgumentException("Unknown array owner: " + owner);
        }
    }

    @Override
    public boolean isSorted() {
        return isSorted(ArrayOwner.PLAYER);
    }

    public boolean isSorted(ArrayOwner owner) {
        int[][] targetNumbers = getNumbers(owner);
        int digitsToCheck = ownerVisibleDigits.get(owner);

        if (digitsToCheck == 5) {
            for (int i = 0; i < targetNumbers.length - 1; i++) {
                long num1 = convertDigitsToLong(targetNumbers[i]);
                long num2 = convertDigitsToLong(targetNumbers[i + 1]);
                if (num1 > num2) {
                    return false;
                }
            }
            return true;
        }

        int currentDigitIndex = digitsToCheck - 1;
        if (currentDigitIndex < 0 || currentDigitIndex >= 4) {
            return false;
        }

        for (int i = 0; i < targetNumbers.length - 1; i++) {
            if (targetNumbers[i][currentDigitIndex] > targetNumbers[i + 1][currentDigitIndex]) {
                return false;
            }
        }
        return true;
    }

    private long convertDigitsToLong(int[] digits) {
        long number = 0;
        for (int digit : digits) {
            if (digit < 0 || digit > 9) {
                throw new IllegalArgumentException("Invalid digit: " + digit);
            }
            number = number * 10 + digit;
        }
        return number;
    }

    @Override
    public void nextStage() {
    }

    public void checkAndAdvanceStageForOwner(ArrayOwner owner) {
        if (isAnimationInProgress(owner)) {
            return;
        }

        int currentDigits = ownerVisibleDigits.get(owner);

        if (currentDigits < 4 && isSorted(owner)) {
            ownerVisibleDigits.put(owner, currentDigits + 1);
            fireDigitRevealedEvent(ownerVisibleDigits.get(owner), owner);

            if (owner == ArrayOwner.FAST_AI) {
                fastAI.stopSorting();
                fastAI.startSorting(this);
            } else if (owner == ArrayOwner.SLOW_AI) {
                slowAI.stopSorting();
                slowAI.startSorting(this);
            }
            checkWinCondition();
        } else if (currentDigits == 4 && isSorted(owner)) {
            if (!ownerFinalStageActive.get(owner)) {
                ownerFinalStageActive.put(owner, true);
                ownerVisibleDigits.put(owner, 5);
                remixNumbersForOwner(owner);
                fireFinalStageEvent(owner);

                if (owner == ArrayOwner.FAST_AI) {
                    fastAI.stopSorting();
                    fastAI.startSorting(this);
                } else if (owner == ArrayOwner.SLOW_AI) {
                    slowAI.stopSorting();
                    slowAI.startSorting(this);
                }
                checkWinCondition();
            }
        } else if (isFinalStage(owner) && isSorted(owner)) {
            checkWinCondition();
        }
    }

    @Override
    public void resetGame() {
        stopAI();
        isGameActive = true;
        swapCount = 0;

        for (ArrayOwner owner : ArrayOwner.values()) {
            ownerVisibleDigits.put(owner, 1);
            ownerFinalStageActive.put(owner, false);
            animationInProgressMap.get(owner).set(false);
            generateRandomNumbersForInitialStage(getNumbers(owner));
        }

        for (ArrayOwner owner : ArrayOwner.values()) {
            fireDigitRevealedEvent(ownerVisibleDigits.get(owner), owner);
        }

        createAI(); // Обновляем стратегии AI при сбросе
        startTime = System.currentTimeMillis();
        startAI();
    }

    private void generateRandomNumbersForInitialStage(int[][] targetArray) {
        for (int i = 0; i < targetArray.length; i++) {
            for (int j = 0; j < 4; j++) {
                targetArray[i][j] = random.nextInt(10);
            }
        }
    }

    private void remixNumbersForOwner(ArrayOwner owner) {
        int[][] targetArray = getNumbers(owner);
        for (int i = 0; i < targetArray.length; i++) {
            List<Integer> digitsList = new ArrayList<>();
            for (int digit : targetArray[i]) {
                digitsList.add(digit);
            }
            Collections.shuffle(digitsList, random);
            for (int j = 0; j < 4; j++) {
                targetArray[i][j] = digitsList.get(j);
            }
        }
    }

    @Override
    public void pauseGame() {
        if (!paused) {
            pauseTime = System.currentTimeMillis();
            paused = true;
            stopAI();
        }
    }

    @Override
    public void resumeGame() {
        if (paused) {
            startTime += (System.currentTimeMillis() - pauseTime);
            paused = false;
            startAI();
        }
    }

    private void checkWinCondition() {
        if (!isGameActive) return;

        boolean playerSortedFinal = isFinalStage(ArrayOwner.PLAYER) && isSorted(ArrayOwner.PLAYER);
        boolean fastAISortedFinal = isFinalStage(ArrayOwner.FAST_AI) && isSorted(ArrayOwner.FAST_AI);
        boolean slowAISortedFinal = isFinalStage(ArrayOwner.SLOW_AI) && isSorted(ArrayOwner.SLOW_AI);

        if (playerSortedFinal) {
            isGameActive = false;
            stopAI();
            fireWinEvent();
        } else if (fastAISortedFinal || slowAISortedFinal) {
            isGameActive = false;
            stopAI();
            fireLoseEvent();
        }
    }

    @Override
    public void startAI() {
        if (!paused && isGameActive) {
            fastAI.startSorting(this);
            slowAI.startSorting(this);
        }
    }

    @Override
    public void stopAI() {
        if (fastAI != null) fastAI.stopSorting();
        if (slowAI != null) slowAI.stopSorting();
    }

    public long getElapsedTime() {
        if (paused) {
            return pauseTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public void addEventListener(BlindSortEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeEventListener(BlindSortEventListener listener) {
        listeners.remove(listener);
    }

    private void fireSwapEvent(int index1, int index2, boolean success, ArrayOwner owner) {
        for (BlindSortEventListener l : listeners) {
            l.onSwap(index1, index2, success, owner);
        }
    }

    private void fireDigitRevealedEvent(int newVisibleDigits, ArrayOwner owner) {
        for (BlindSortEventListener l : listeners) {
            l.onDigitRevealed(newVisibleDigits, owner);
        }
    }

    private void fireFinalStageEvent(ArrayOwner owner) {
        for (BlindSortEventListener l : listeners) {
            l.onFinalStageStarted(owner);
        }
    }

    private void fireWinEvent() {
        for (BlindSortEventListener l : listeners) {
            l.onWin();
        }
    }

    private void fireLoseEvent() {
        for (BlindSortEventListener l : listeners) {
            l.onLose();
        }
    }

    @Override
    public boolean isFinalStage() {
        return isFinalStage(ArrayOwner.PLAYER);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setAnimationInProgress(boolean inProgress, ArrayOwner owner) {
        animationInProgressMap.get(owner).set(inProgress);
        if (!inProgress) {
            checkAndAdvanceStageForOwner(owner);
        }
    }

    @Override
    public boolean isAnimationInProgress() {
        return animationInProgressMap.get(ArrayOwner.PLAYER).get();
    }

    @Override
    public boolean isAnimationInProgress(ArrayOwner owner) {
        return animationInProgressMap.get(owner).get();
    }

    @Override
    public int getSwapCount() {
        return swapCount;
    }

    @Override
    public boolean isGameActive() {
        return isGameActive;
    }

    public int[][] getPlayerNumbers() { return playerNumbers; }
    public int[][] getFastAINumbersInternal() { return fastAINumbers; }
    public int[][] getSlowAINumbersInternal() { return slowAINumbers; }

    public int getArrayLength() {
        return playerNumbers.length;
    }
}