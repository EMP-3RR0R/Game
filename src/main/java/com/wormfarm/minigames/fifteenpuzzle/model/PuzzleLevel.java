package com.wormfarm.minigames.fifteenpuzzle.model;

// Заготовка для будущих уровней
public class PuzzleLevel {
    private final String name;
    private final int size;
    // Можно добавить параметры: таймер, рекорды, модификаторы и т.д.

    public PuzzleLevel(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}