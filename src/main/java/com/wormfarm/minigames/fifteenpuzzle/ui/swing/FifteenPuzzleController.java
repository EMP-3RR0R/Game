package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.api.FifteenPuzzleGame;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FifteenPuzzleController {
    private final FifteenPuzzleGame game;
    private final FifteenPuzzleVisualizer visualizer;
    private final Component parentComponent;

    public FifteenPuzzleController(FifteenPuzzleGame game, FifteenPuzzleVisualizer visualizer, Component parentComponent) {
        this.game = game;
        this.visualizer = visualizer;
        this.parentComponent = parentComponent;

        visualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tileSize = visualizer.getWidth() / game.getSize();
                int row = e.getY() / tileSize;
                int col = e.getX() / tileSize;
                game.moveTile(row, col);
                visualizer.setBoard(game.getBoardCopy());
            }
        });

        game.addEventListener(new PuzzleEventListener() {
            @Override
            public void onMove(int x, int y, boolean success) {
                // Можно добавить анимации, звуки, эффекты
            }

            @Override
            public void onWin() {
                JOptionPane.showMessageDialog(parentComponent, "Вы победили!");
            }
        });
    }
}