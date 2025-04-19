package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.gui.base.BaseInternalFrame;

import javax.swing.*;
import java.awt.*;

public class FifteenPuzzleFrame extends BaseInternalFrame {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;

    public FifteenPuzzleFrame() {
        super("puzzle.title", true, true, true, true);

        setLayout(new BorderLayout());

        ClassicFifteenPuzzleLogic logic = new ClassicFifteenPuzzleLogic(SIZE);
        FifteenPuzzleVisualizer visualizer = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE);
        new FifteenPuzzleController(logic, visualizer, this);

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> {
            logic.resetBoard();
            visualizer.setBoard(logic.getBoardCopy());
        });

        add(visualizer, BorderLayout.CENTER);
        add(resetButton, BorderLayout.SOUTH);

        setSize(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 80);
        setPreferredSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 80));
        setMinimumSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 80));
        setMaximumSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 80));

        visualizer.setBoard(logic.getBoardCopy());
    }

    @Override
    protected String getTitleKey() {
        return "puzzle.title";
    }

    @Override
    protected void updateComponents() {
        // Обновление локализации и других компонентов, если потребуется
    }
}