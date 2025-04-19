package com.wormfarm.legacy.minigame;

import com.wormfarm.legacy.gui.BaseInternalFrame;

import javax.swing.*;
import java.awt.*;

public class FifteenPuzzleFrame extends BaseInternalFrame {

    private final FifteenPuzzleVisualizer visualizer;

    public FifteenPuzzleFrame() {
        super("puzzle.title", true, true, true, true);
        this.visualizer = new FifteenPuzzleVisualizer(4); // Поле 4x4
        setLayout(new BorderLayout());
        add(visualizer, BorderLayout.CENTER);

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> visualizer.resetGame());
        add(resetButton, BorderLayout.SOUTH);

        setSize(500, 500);
    }

    @Override
    protected String getTitleKey() {
        return "puzzle.title";
    }

    @Override
    protected void updateComponents() {
        // Обновление локализации или компонентов (если нужно)
    }
}