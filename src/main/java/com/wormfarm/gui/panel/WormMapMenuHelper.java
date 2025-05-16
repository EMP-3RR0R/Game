package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.dialog.PauseMenuDialog;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;

public class WormMapMenuHelper {
    private final WormMapPanel panel;
    private final WormState worm;
    private final WormStatsManager statsManager;
    private final UserSettings settings;

    public WormMapMenuHelper(WormMapPanel panel, WormState worm, WormStatsManager statsManager) {
        this(panel, worm, statsManager, null);
    }

    public WormMapMenuHelper(WormMapPanel panel, WormState worm, WormStatsManager statsManager, UserSettings settings) {
        this.panel = panel;
        this.worm = worm;
        this.statsManager = statsManager;
        this.settings = settings;
    }

    public void showPauseMenu(JFrame owner, Runnable onResume, Runnable onExitToMenu, Runnable onExitToDesktop, Runnable onLanguageChanged) {
        JDesktopPane desktopPane = panel.getDesktopPane();
        GameSessionManager gsm = panel.getGameSessionManager();
        PauseMenuDialog pauseFrame = new PauseMenuDialog(
                owner,
                onResume,
                onExitToMenu,
                onExitToDesktop,
                worm,
                statsManager,
                panel.getTargetX(),
                panel.getTargetY(),
                settings,
                onLanguageChanged,
                panel,
                gsm
        );
        if (desktopPane != null) {
            int x = (desktopPane.getWidth() - pauseFrame.getWidth()) / 2;
            int y = (desktopPane.getHeight() - pauseFrame.getHeight()) / 2;
            pauseFrame.setLocation(Math.max(x, 0), Math.max(y, 0));
            desktopPane.add(pauseFrame, JLayeredPane.POPUP_LAYER);
            try {
                pauseFrame.setSelected(true);
            } catch (Exception ignored) {}
            pauseFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(owner, "Пауза работает только с desktopPane!");
        }
    }
}