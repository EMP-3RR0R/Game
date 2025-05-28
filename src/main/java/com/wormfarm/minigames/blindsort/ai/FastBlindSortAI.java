package com.wormfarm.minigames.blindsort.ai;

import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;

public class FastBlindSortAI extends BaseBlindSortAI {
    public FastBlindSortAI(ArrayOwner owner) {
        super(2000, "Быстрый_ИИ", owner, null, 0.0);
    }
}