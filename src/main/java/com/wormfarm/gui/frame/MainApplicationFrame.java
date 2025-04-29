package com.wormfarm.gui.frame;

import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.dialog.LoadGameDialog;
import com.wormfarm.gui.panel.MainMenuPanel;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class MainApplicationFrame extends JFrame {
    public static final int MAP_WIDTH = 800;
    public static final int MAP_HEIGHT = 800;

    protected final JDesktopPane desktopPane = new JDesktopPane();
    protected ResourceBundle messages;

    private WormState currentWormState;
    private WormStats currentWormStats;
    private EventMapModel currentEventMap;
    private WormMapPanel currentMapPanel;

    public MainApplicationFrame() {
        AppLocale.setLocale(AppLocale.detectDefaultLocaleLang());
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());

        setTitle("Worm Farm");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        MainApplicationFrame.this,
                        messages.getString("confirm.close.message"),
                        messages.getString("confirm.close.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        setJMenuBar(createMenuBar());
        setResizable(false);
        setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMinimumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMaximumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        pack();
        setLocationRelativeTo(null);

        showMainMenu();
    }

    public void showMainMenu() {
        setContentPane(new MainMenuPanel(
                this::continueGame,
                this::startNewGame,
                this::loadGame,
                this::openSettings, // только передаем обработчик
                this::exitGame
        ));
        revalidate();
        repaint();
    }

    public void startNewGame() {
        currentWormState = new WormState(100, 100, 0);
        currentEventMap = new EventMapModel();
        currentWormStats = new WormStats(0);

        WormStatsManager statsManager = new WormStatsManager(currentWormStats);

        currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager);
        currentMapPanel.setOnExitToMenu(this::showMainMenu);

        setContentPane(currentMapPanel);
        revalidate();
        repaint();
        currentMapPanel.requestFocusInWindow();
    }

    public void continueGame() {
        if (currentWormState != null && currentWormStats != null) {
            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);

            setContentPane(currentMapPanel);
            revalidate();
            repaint();
            currentMapPanel.requestFocusInWindow();
        } else {
            List<String> saves = WormSaveManager.listSaves();
            if (saves.isEmpty()) {
                JOptionPane.showMessageDialog(this, messages.getString("no.saves.to.continue"));
                return;
            }
            loadSpecificGame(saves.get(saves.size() - 1));
        }
    }

    public void loadGame() {
        List<String> saves = WormSaveManager.listSaves();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(this, messages.getString("no.saves.available"));
            return;
        }

        LoadGameDialog dialog = new LoadGameDialog(this, saves);
        dialog.setVisible(true);

        String selectedName = dialog.getSelectedName();
        if (selectedName != null) {
            loadSpecificGame(selectedName);
        }
    }

    private void loadSpecificGame(String saveName) {
        try {
            currentWormState = new WormState(100, 100, 0);
            currentEventMap = new EventMapModel();
            currentWormStats = new WormStats(0);

            WormSaveManager.load(currentWormState, currentWormStats, saveName);

            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);

            setContentPane(currentMapPanel);
            revalidate();
            repaint();
            currentMapPanel.requestFocusInWindow();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    messages.getString("error.load.save") + ": " + e.getMessage());
        }
    }

    public void openSettings() {
        // Здесь больше ничего не делаем, ответственность полностью на MainMenuPanel
    }

    public void exitGame() {
        System.exit(0);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // Можно добавить локализованные пункты меню здесь, если нужно
        return menuBar;
    }

    @Override
    public void dispose() {
        if (currentMapPanel != null) {
            currentMapPanel.dispose();
        }
        super.dispose();
    }
}