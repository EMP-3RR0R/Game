package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.api.FifteenPuzzleGame;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.core.logic.WormStatsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FifteenPuzzleController {
    private final FifteenPuzzleGame game;
    private final FifteenPuzzleVisualizer visualizer;
    private final Component parentComponent;
    private final WormStatsManager wormStatsManager;
    private boolean gameOver = false;

    private boolean isPaused = false;

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public FifteenPuzzleController(FifteenPuzzleGame game, FifteenPuzzleVisualizer visualizer, Component parentComponent, WormStatsManager wormStatsManager) {
        this.game = game;
        this.visualizer = visualizer;
        this.parentComponent = parentComponent;
        this.wormStatsManager = wormStatsManager;

        visualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isPaused || gameOver || visualizer.isAnimating()) return;
                if (gameOver || visualizer.isAnimating()) return;
                int tileSize = visualizer.getWidth() / game.getSize();
                int row = e.getY() / tileSize;
                int col = e.getX() / tileSize;
                if (row >= 0 && row < game.getSize() && col >= 0 && col < game.getSize()) {
                    int[][] before = game.getBoardCopy();
                    boolean moved = game.moveTile(row, col);
                    int[][] after = game.getBoardCopy();
                    if (moved) {
                        SoundUtils.playSound("/sounds/click.wav");
                        // Найти, какой тайл сдвинулся и куда
                        outer:
                        for (int r = 0; r < game.getSize(); r++) {
                            for (int c = 0; c < game.getSize(); c++) {
                                if (before[r][c] != 0 && after[r][c] == 0) {
                                    int value = before[r][c];
                                    for (int rr = 0; rr < game.getSize(); rr++) {
                                        for (int cc = 0; cc < game.getSize(); cc++) {
                                            if (after[rr][cc] == value) {
                                                visualizer.animateMove(value, r, c, rr, cc, () -> {
                                                    visualizer.setBoard(after);
                                                    visualizer.repaint();
                                                });
                                                break outer;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        int value = before[row][col];
                        if (value != 0) {
                            visualizer.animateShake(value);
                            SoundUtils.playSound("/sounds/click.wav");
                        }
                    }
                }
            }
        });

        game.addEventListener(new PuzzleEventListener() {
            @Override
            public void onMove(int x, int y, boolean success) {
                // Не обновляем здесь визуализатор — этим занимается animateMove
            }

            @Override
            public void onWin() {
                gameOver = true;
                SwingUtilities.invokeLater(FifteenPuzzleController.this::handleWinOnEdt);
            }
        });
    }

    // Новый package-private метод для обработки победы на EDT.
    void handleWinOnEdt() {
        if (wormStatsManager != null) {
            wormStatsManager.addCoins(10);
        }

        int moves = 0;
        long ms = 0;
        if (game instanceof ClassicFifteenPuzzleLogic) {
            moves = ((ClassicFifteenPuzzleLogic) game).getMoveCount();
            ms = ((ClassicFifteenPuzzleLogic) game).getElapsedTimeMillis();
        }
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        String stats = String.format("Ходы: %d\nВремя: %02d:%02d", moves, min, sec);

        SoundUtils.playSound("/sounds/win.wav");

        Object[] options = {"Сыграть ещё!", "Вернуться к приключениям!"};
        int choice = JOptionPane.showOptionDialog(
                parentComponent,
                "<html>Вы победили!<br><br><pre>" + stats + "</pre></html>",
                "Пятнашки",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) {
            // Сыграть ещё: новая доска и новый спрайт!
            game.resetBoard();
            if (visualizer != null) {
                visualizer.resetPuzzleImage();
                visualizer.setBoard(game.getBoardCopy());
                visualizer.repaint();
            }
            gameOver = false;
        } else if (choice == 1) {
            // Вернуться к приключениям: закрыть JDialog через Frame
            if (parentComponent instanceof FifteenPuzzleFrame) {
                JDialog dlg = ((FifteenPuzzleFrame) parentComponent).getParentDialog();
                if (dlg != null) dlg.dispose();
            }
        }
    }

    public boolean isPausedGame() {
        return isPaused;
    }
}