package com.wormfarm.minigames.blindsort.ai;

import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;

public interface BlindSortAI {
    void startSorting(BlindSortLogic gameLogic);
    void stopSorting();
    boolean isSorting();
}