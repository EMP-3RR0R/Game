package com.wormfarm.minigames.fifteenpuzzle.api;

import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;

public interface FifteenPuzzleGame {
    int getSize();
    int[][] getBoardCopy();
    boolean moveTile(int x, int y);
    boolean isSolved();
    void resetBoard();
    void addEventListener(PuzzleEventListener listener);
    void removeEventListener(PuzzleEventListener listener);
}