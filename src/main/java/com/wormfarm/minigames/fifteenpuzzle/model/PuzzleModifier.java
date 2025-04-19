package com.wormfarm.minigames.fifteenpuzzle.model;

// Заготовка для будущих модификаторов испытания
public interface PuzzleModifier {
    void apply(int[][] board);
    String getDescription();
}