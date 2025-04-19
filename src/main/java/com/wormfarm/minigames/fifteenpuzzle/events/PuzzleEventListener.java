package com.wormfarm.minigames.fifteenpuzzle.events;

public interface PuzzleEventListener {
    void onMove(int x, int y, boolean success);
    void onWin();
    // Для расширения: onLose, onError и т.д.
}