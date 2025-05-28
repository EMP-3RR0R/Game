package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.minigames.blindsort.api.BlindSortGame;
import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import com.wormfarm.util.SoundUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ResourceBundle;

public class BlindSortController {
    private final BlindSortLogic game;
    private final BlindSortVisualizer visualizer;
    private final WormStatsManager statsManager;
    private final Component parentComponent;
    private final ResourceBundle messages;
    private boolean gameOver = false;

    private Integer selectedIndexForPlayer = null;

    public BlindSortController(BlindSortLogic game, BlindSortVisualizer visualizer,
                               Component parentComponent, WormStatsManager wormStatsManager,
                               ResourceBundle messages) {
        this.game = game;
        this.visualizer = visualizer;
        this.statsManager = wormStatsManager;
        this.parentComponent = parentComponent;
        this.messages = messages;

        this.visualizer.setControllerCallback(this);
        setupListeners();
    }

    private void setupListeners() {
        visualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver || game.isPaused() || game.isAnimationInProgress(ArrayOwner.PLAYER)) {
                    return;
                }

                int y = e.getY();
                int playerCubesYStart = (0 * (visualizer.getCubeHeight() + visualizer.getRowSpacing() + visualizer.getLabelOffset()))
                        + visualizer.getRowPadding() + visualizer.getLabelOffset() + 5;
                int playerCubesYEnd = playerCubesYStart + visualizer.getCubeHeight();

                if (y >= playerCubesYStart && y <= playerCubesYEnd) {
                    int x = e.getX();
                    int cubeClickArea = visualizer.getCubeWidth() + visualizer.getCubeSpacing();
                    int index = (x - visualizer.getCubeSpacing()) / cubeClickArea;

                    if (index >= 0 && index < game.getNumbersCopy(ArrayOwner.PLAYER).length) {
                        handlePlayerCubeClick(index);
                    }
                }
            }
        });

        visualizer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int y = e.getY();
                int playerCubesYStart = (0 * (visualizer.getCubeHeight() + visualizer.getRowSpacing() + visualizer.getLabelOffset()))
                        + visualizer.getRowPadding() + visualizer.getLabelOffset() + 5;
                int playerCubesYEnd = playerCubesYStart + visualizer.getCubeHeight();

                if (y >= playerCubesYStart && y <= playerCubesYEnd) {
                    int x = e.getX();
                    int cubeClickArea = visualizer.getCubeWidth() + visualizer.getCubeSpacing();
                    int hoverIndex = (x - visualizer.getCubeSpacing()) / cubeClickArea;
                    if (hoverIndex >= 0 && hoverIndex < game.getNumbersCopy(ArrayOwner.PLAYER).length) {
                        visualizer.handlePlayerHover(hoverIndex);
                    } else {
                        visualizer.handlePlayerHover(-1);
                    }
                } else {
                    visualizer.handlePlayerHover(-1);
                }
            }
        });

        game.addEventListener(new BlindSortEventListener() {
            @Override
            public void onWin() {
                gameOver = true;
                if (statsManager != null) {
                    statsManager.addCoins(10);
                }
                SoundUtils.playSound("/sounds/win.wav");
                showEndDialog(true);
            }

            @Override
            public void onLose() {
                gameOver = true;
                SoundUtils.playSound("/sounds/lose.wav");
                showEndDialog(false);
            }

            @Override
            public void onSwap(int i1, int i2, boolean success, ArrayOwner owner) {
            }

            @Override
            public void onDigitRevealed(int digits, ArrayOwner owner) {
            }

            @Override
            public void onFinalStageStarted(ArrayOwner owner) {
            }
        });
    }

    private void handlePlayerCubeClick(int clickedIndex) {
        SoundUtils.playSound("/sounds/click.wav");

        if (game.isAnimationInProgress(ArrayOwner.PLAYER) || !game.isGameActive() || game.isPaused()) {
            return;
        }

        if (selectedIndexForPlayer != null && selectedIndexForPlayer == clickedIndex) {
            visualizer.setPlayerSelectedIndex(null);
            selectedIndexForPlayer = null;
        } else if (selectedIndexForPlayer == null) {
            selectedIndexForPlayer = clickedIndex;
            visualizer.setPlayerSelectedIndex(clickedIndex);
        } else {
            boolean swapSuccessful = game.swapNumbers(selectedIndexForPlayer, clickedIndex);

            if (swapSuccessful) {
                visualizer.setPlayerSelectedIndex(null);
                game.setAnimationInProgress(true, ArrayOwner.PLAYER);
                visualizer.startPlayerSwapAnimation(selectedIndexForPlayer, clickedIndex);
            } else {
                visualizer.setPlayerSelectedIndex(null);
            }
            selectedIndexForPlayer = null;
        }
    }

    public void handlePlayerSwapAnimationFinished() {
        selectedIndexForPlayer = null;
        visualizer.setPlayerSelectedIndex(null);
        visualizer.updateNumbersCache(ArrayOwner.PLAYER);
        game.setAnimationInProgress(false, ArrayOwner.PLAYER);
    }

    private void showEndDialog(boolean won) {
        String titleKey = won ? "blindsort.win.title" : "blindsort.lose.title";
        String messageKey = won ? "blindsort.win.message" : "blindsort.lose.message";
        String againKey = won ? "blindsort.win.play_again" : "blindsort.lose.try_again";

        Object[] options = {
                messages.getString(againKey),
                messages.getString("blindsort.win.return")
        };

        int choice = JOptionPane.showOptionDialog(
                parentComponent,
                messages.getString(messageKey),
                messages.getString(titleKey),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            game.resetGame();
            visualizer.resetAnimation();
            gameOver = false;
        } else {
            if (parentComponent instanceof Window) {
                ((Window) parentComponent).dispose();
            } else if (parentComponent instanceof JInternalFrame) {
                ((JInternalFrame) parentComponent).dispose();
            }
        }
    }
}