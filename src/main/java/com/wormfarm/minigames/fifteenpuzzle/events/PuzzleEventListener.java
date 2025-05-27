package com.wormfarm.minigames.fifteenpuzzle.events;

public interface PuzzleEventListener {
    void onMove(int x, int y, boolean success);
    void onWin();
}