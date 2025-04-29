package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;

import javax.swing.*;
import java.util.List;

public class WormMapMenuHelper {
    private final WormMapPanel panel;
    private final WormState worm;
    private final WormStatsManager statsManager;

    public WormMapMenuHelper(WormMapPanel panel, WormState worm, WormStatsManager statsManager) {
        this.panel = panel;
        this.worm = worm;
        this.statsManager = statsManager;
    }

    public void showPauseMenu(JFrame owner, Runnable onExitToMenu, Runnable onLanguageChanged) {
        com.wormfarm.gui.dialog.PauseMenuDialog dlg = new com.wormfarm.gui.dialog.PauseMenuDialog(
                owner,
                panel::requestFocusInWindow,
                () -> { if (onExitToMenu != null) onExitToMenu.run(); },
                () -> System.exit(0),
                this::saveGameWithName,
                this::loadGameWithName,
                !com.wormfarm.core.logic.WormSaveManager.listSaves().isEmpty(),
                onLanguageChanged
        );
        dlg.setVisible(true);
    }

    private void saveGameWithName() {
        List<String> saves = com.wormfarm.core.logic.WormSaveManager.listSaves();
        com.wormfarm.gui.dialog.SaveGameDialog dlg = new com.wormfarm.gui.dialog.SaveGameDialog((JFrame) SwingUtilities.getWindowAncestor(panel), saves);
        dlg.setVisible(true);
        String saveName = dlg.getSelectedName();
        if (saveName != null) {
            try {
                com.wormfarm.core.logic.WormSaveManager.save(worm, statsManager.getStats(), saveName);
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
                com.wormfarm.core.logic.WormSaveManager.load(worm, statsManager.getStats(), saveName);
                JOptionPane.showMessageDialog(panel, "Игра '" + saveName + "' загружена!");
                panel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}