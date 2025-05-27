package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.api.FifteenPuzzleGame;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.util.SoundUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class FifteenPuzzleController {
    private final FifteenPuzzleGame game;
    private final FifteenPuzzleVisualizer visualizer;
    private final Component parentComponent;
    private final WormStatsManager wormStatsManager;
    private final FifteenPuzzleFrame frame;
    boolean gameOver = false;
    private boolean isPaused = false;

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public FifteenPuzzleController(FifteenPuzzleGame game, FifteenPuzzleVisualizer visualizer, Component parentComponent, WormStatsManager wormStatsManager, FifteenPuzzleFrame frame) {
        this.game = game;
        this.visualizer = visualizer;
        this.parentComponent = parentComponent;
        this.wormStatsManager = wormStatsManager;
        this.frame = frame;

        visualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isPaused || gameOver || visualizer.isAnimating()) return;
                int tileSize = visualizer.getWidth() / game.getSize();
                int row = e.getY() / tileSize;
                int col = e.getX() / tileSize;
                if (row >= 0 && row < game.getSize() && col >= 0 && col < game.getSize()) {
                    int[][] before = game.getBoardCopy();
                    boolean moved = game.moveTile(row, col);
                    int[][] after = game.getBoardCopy();
                    if (moved) {
                        SoundUtils.playSound("/sounds/click.wav");
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
            public void onMove(int x, int y, boolean success) {}

            @Override
            public void onWin() {
                gameOver = true;
                SwingUtilities.invokeLater(FifteenPuzzleController.this::handleWinOnEdt);
            }
        });
    }

    void handleWinOnEdt() {
        ResourceBundle messages = frame.getMessages();

        if (wormStatsManager != null) {
            wormStatsManager.addCoins(10);
        }
        int moves = 0;
        long ms = 0;
        if (game instanceof ClassicFifteenPuzzleLogic) {
            moves = ((ClassicFifteenPuzzleLogic) game).getLastWinMoveCount();
            ms = ((ClassicFifteenPuzzleLogic) game).getLastWinElapsedTime();
        }
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        String stats = MessageFormat.format(
                "{0}: {1}\n{2}: {3}:{4}",
                messages.getString("puzzle.moves"),
                moves,
                messages.getString("puzzle.time"),
                String.format("%02d", min),
                String.format("%02d", sec)
        );

        SoundUtils.playSound("/sounds/win.wav");

        Object[] options = {
                messages.getString("puzzle.win.play.again"),
                messages.getString("puzzle.win.return")
        };
        int choice = JOptionPane.showOptionDialog(
                parentComponent,
                "<html>" + messages.getString("puzzle.win.message") + "<br><br><pre>" + stats + "</pre></html>",
                messages.getString("puzzle.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) {
            game.resetBoard();
            if (visualizer != null) {
                visualizer.resetPuzzleImage();
                visualizer.setBoard(game.getBoardCopy());
                visualizer.repaint();
            }
            gameOver = false;
        } else if (choice == 1) {
            if (parentComponent instanceof FifteenPuzzleFrame) {
                ((FifteenPuzzleFrame) parentComponent).dispose();
            } else if (parentComponent instanceof JInternalFrame) {
                ((JInternalFrame) parentComponent).dispose();
            }
        }
    }

    public boolean isPausedGame() {
        return isPaused;
    }
}