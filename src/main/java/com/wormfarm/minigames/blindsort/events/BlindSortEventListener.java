package com.wormfarm.minigames.blindsort.events;

import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;

public interface BlindSortEventListener {
    void onWin();
    void onLose();
    void onSwap(int i1, int i2, boolean success, ArrayOwner owner);
    void onDigitRevealed(int digits, ArrayOwner owner);
    void onFinalStageStarted(ArrayOwner owner);
}