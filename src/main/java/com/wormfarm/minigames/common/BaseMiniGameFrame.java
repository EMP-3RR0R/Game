package com.wormfarm.minigames.common;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.base.BaseInternalFrame;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class BaseMiniGameFrame extends BaseInternalFrame {
    protected final WormStatsManager wormStatsManager;

    public BaseMiniGameFrame(String titleKey, WormStatsManager wormStatsManager) {
        super(titleKey, true, true, true, true);
        this.wormStatsManager = wormStatsManager;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showPauseMenu();
                }
            }
        });
        setFocusable(true);
    }

    public void showPauseMenu() {
        pauseGame();
        int option = JOptionPane.showOptionDialog(
                this,
                "Игра на паузе. Что вы хотите сделать?",
                "Пауза",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Продолжить", "Завершить игру"},
                "Продолжить"
        );
        if (option == 0) {
            resumeGame();
        } else if (option == 1) {
            endGame();
            dispose();
        }
    }

    public abstract void startGame();
    public abstract void pauseGame();
    public abstract void resumeGame();
    public abstract void endGame();
}