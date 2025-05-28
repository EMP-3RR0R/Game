package com.wormfarm.minigames.blindsort.api;

import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;

public interface BlindSortGame {
    int[][] getNumbersCopy();
    int getVisibleDigits();
    int getVisibleDigitsForOwner(ArrayOwner owner);
    boolean isFinalStage(ArrayOwner owner);
    boolean swapNumbers(int index1, int index2);
    boolean isSorted();
    void nextStage();
    void resetGame();
    void pauseGame();
    void resumeGame();
    void startAI();
    void stopAI();
    long getElapsedTime();
    void addEventListener(BlindSortEventListener listener);
    void removeEventListener(BlindSortEventListener listener);
    boolean isFinalStage();
    int getSwapCount();
    boolean isGameActive();

    boolean isPaused();

    void setAnimationInProgress(boolean inProgress, ArrayOwner owner);

    boolean isAnimationInProgress();
    boolean isAnimationInProgress(ArrayOwner owner);
}