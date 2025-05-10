package com.wormfarm.minigames.common;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.base.BaseInternalFrame;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import com.wormfarm.settings.AppLocale;

public abstract class BaseMiniGameFrame extends BaseInternalFrame {
    protected final WormStatsManager wormStatsManager;
    protected ResourceBundle messages;

    public BaseMiniGameFrame(String titleKey, WormStatsManager wormStatsManager) {
        super(getLocalizedString(titleKey), true, true, true, true);
        this.wormStatsManager = wormStatsManager;
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
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
                messages.getString("minigame.pause.message"),
                messages.getString("minigame.pause.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        messages.getString("minigame.pause.continue"),
                        messages.getString("minigame.pause.quit")
                },
                messages.getString("minigame.pause.continue")
        );
        if (option == 0) {
            resumeGame();
        } else if (option == 1) {
            endGame();
            dispose();
        }
    }

    public static String getLocalizedString(String key) {
        try {
            ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
            return messages.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public void updateLocale() {
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        setTitle(getLocalizedString(getTitle()));
    }

    public abstract void startGame();
    public abstract void pauseGame();
    public abstract void resumeGame();
    public abstract void endGame();
}