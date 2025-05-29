package com.wormfarm.minigames.blindsort.ai;

import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import com.wormfarm.minigames.blindsort.ai.strategy.AISortingStrategy;
import java.util.Optional;
import java.util.Random;

public abstract class BaseBlindSortAI implements BlindSortAI, Runnable {
    protected final Random random = new Random();
    protected BlindSortLogic game;
    protected final ArrayOwner owner;
    protected volatile boolean running;
    protected final int baseDelay;
    protected final String aiName;

    protected AISortingStrategy strategy;
    protected double errorChance;

    Thread sortingThread;

    public BaseBlindSortAI(int baseDelay, String aiName, ArrayOwner owner, AISortingStrategy strategy, double errorChance) {
        this.baseDelay = baseDelay;
        this.aiName = aiName;
        this.owner = owner;
        this.strategy = strategy;
        this.errorChance = errorChance;
    }

    @Override
    public void startSorting(BlindSortLogic game) {
        this.game = game;
        if (sortingThread != null && sortingThread.isAlive()) {
            stopSorting();
        }
        this.running = true;
        sortingThread = new Thread(this, owner.name() + "_" + aiName + "_Thread");
        sortingThread.start();
    }

    @Override
    public void stopSorting() {
        running = false;
        if (sortingThread != null) {
            sortingThread.interrupt();
            try {
                sortingThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isSorting() {
        return running;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (game.isSorted(owner)) {
                    Thread.sleep(100);
                    continue;
                }

                if (game.isPaused() || game.isAnimationInProgress(owner)) {
                    Thread.sleep(100);
                    continue;
                }

                Thread.sleep(baseDelay + random.nextInt(baseDelay / 2));
                if (running && !game.isPaused() && !game.isAnimationInProgress(owner)) {
                    makeMove();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    protected void makeMove() {
        int[][] numbers = getMyNumbers();
        int visibleDigits = getVisibleDigits();

        Optional<int[]> move = strategy.getNextMove(numbers, visibleDigits, errorChance);

        if (move.isPresent()) {
            int[] indices = move.get();
            game.swapNumbersInternal(indices[0], indices[1], owner);
        }
    }

    protected int[][] getMyNumbers() {
        return game.getNumbersCopy(owner);
    }

    protected int getVisibleDigits() {
        return game.getVisibleDigitsForOwner(owner);
    }

    public String getAIName() {
        return aiName;
    }

    public void setStrategy(AISortingStrategy strategy, double errorChance) {
        this.strategy = strategy;
        this.errorChance = errorChance;
    }
}