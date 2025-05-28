    package com.wormfarm.minigames.blindsort.ai.strategy;

    import java.util.Optional;

    public interface AISortingStrategy {
        Optional<int[]> getNextMove(int[][] numbers, int visibleDigits, double errorChance);

        String getName();
    }