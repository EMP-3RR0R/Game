package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.dialog.PauseMenuDialog;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.util.List;

public class WormMapMenuHelper {
    private final WormMapPanel panel;
    private final WormState worm;
    private final WormStatsManager statsManager;
    private final UserSettings settings;

    // Конструктор без settings — присваиваем null или создаём дефолтные настройки
    public WormMapMenuHelper(WormMapPanel panel, WormState worm, WormStatsManager statsManager) {
        this(panel, worm, statsManager, null); // делегируем в основной конструктор
    }

    // Основной конструктор
    public WormMapMenuHelper(WormMapPanel panel, WormState worm, WormStatsManager statsManager, UserSettings settings) {
        this.panel = panel;
        this.worm = worm;
        this.statsManager = statsManager;
        this.settings = settings;
    }

    public void showPauseMenu(JFrame owner, Runnable onResume, Runnable onExitToMenu, Runnable onLanguageChanged) {
        JDesktopPane desktopPane = panel.getDesktopPane();
        PauseMenuDialog pauseFrame = new PauseMenuDialog(
                owner,
                onResume,
                () -> { if (onExitToMenu != null) onExitToMenu.run(); },
                () -> System.exit(0),
                this::saveGameWithName,
                this::loadGameWithName,
                !com.wormfarm.core.logic.WormSaveManager.listSaves().isEmpty(),
                onLanguageChanged,
                settings // обязательно передаём настройки!
        );
        if (desktopPane != null) {
            int x = (desktopPane.getWidth() - pauseFrame.getWidth())/2;
            int y = (desktopPane.getHeight() - pauseFrame.getHeight())/2;
            pauseFrame.setLocation(Math.max(x,0), Math.max(y,0));
            desktopPane.add(pauseFrame, JLayeredPane.POPUP_LAYER);
            try { pauseFrame.setSelected(true); } catch (Exception ignored) {}
            pauseFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(owner, "Пауза работает только с desktopPane!");
        }
    }

    private void saveGameWithName() {
        List<String> saves = com.wormfarm.core.logic.WormSaveManager.listSaves();
        com.wormfarm.gui.dialog.SaveGameDialog dlg = new com.wormfarm.gui.dialog.SaveGameDialog((JFrame) SwingUtilities.getWindowAncestor(panel), saves);
        dlg.setVisible(true);
        String saveName = dlg.getSelectedName();
        if (saveName != null) {
            try {
                com.wormfarm.core.logic.WormSaveManager.save(
                        worm,
                        statsManager.getStats(),
                        panel.getTargetX(),
                        panel.getTargetY(),
                        saveName
                );
                JOptionPane.showMessageDialog(panel, "Игра сохранена как '" + saveName + "'!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Ошибка сохранения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGameWithName() {
        List<String> saves = com.wormfarm.core.logic.WormSaveManager.listSaves();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Нет сохранений для загрузки!");
            return;
        }
        com.wormfarm.gui.dialog.LoadGameDialog dlg = new com.wormfarm.gui.dialog.LoadGameDialog((JFrame) SwingUtilities.getWindowAncestor(panel), saves);
        dlg.setVisible(true);
        String saveName = dlg.getSelectedName();
        if (saveName != null) {
            try {
                com.wormfarm.core.logic.WormSaveManager.load(
                        worm,
                        statsManager.getStats(),
                        panel::setTarget,
                        saveName
                );
                JOptionPane.showMessageDialog(panel, "Игра '" + saveName + "' загружена!");
                panel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}