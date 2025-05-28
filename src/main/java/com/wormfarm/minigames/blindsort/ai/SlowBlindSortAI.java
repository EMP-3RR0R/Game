package com.wormfarm.minigames.blindsort.ai;

import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;

public class SlowBlindSortAI extends BaseBlindSortAI {
    public SlowBlindSortAI(ArrayOwner owner) {
        super(3500, "Медленный_ИИ", owner, null, 0.0);
    }
}