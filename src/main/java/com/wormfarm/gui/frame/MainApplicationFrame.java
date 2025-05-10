package com.wormfarm.gui.frame;

import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.dialog.LoadGameDialog;
import com.wormfarm.gui.panel.MainMenuPanel;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.gui.panel.WormMapMenuHelper;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
    private UserSettings settings;

    public MainApplicationFrame() {
        // 1. Загрузка настроек пользователя
        try {
            File settingsFile = new File("user.settings");
            if (settingsFile.exists()) {
                settings = UserSettings.load(settingsFile);
            } else {
                settings = new UserSettings();
                // Можно инициализировать язык автодетектом если еще нет значения
                if (settings.getLanguage() == null || settings.getLanguage().isEmpty()) {
                    String defaultLang = AppLocale.detectDefaultLocaleLang();
                    settings.setLanguage(defaultLang);
                    UserSettings.save(settings, settingsFile);
                }
            }
        } catch (Exception e) {
            settings = new UserSettings();
            if (settings.getLanguage() == null || settings.getLanguage().isEmpty()) {
                String defaultLang = AppLocale.detectDefaultLocaleLang();
                settings.setLanguage(defaultLang);
            }
        }

        // 2. Устанавливаем локаль приложения по настройкам пользователя
        AppLocale.setLocale(settings.getLanguage());

        // 3. Только теперь инициализируем messages
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

        // Обеспечить правильное масштабирование карты при ресайзе desktopPane (если вдруг будет resizable=true)
        desktopPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (currentMapPanel != null) {
                    currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
                }
            }
        });

        showMainMenu();
    }

    public void showMainMenu() {
        List<String> saves = WormSaveManager.listSaves();
        boolean hasSaves = !saves.isEmpty();
        setContentPane(new MainMenuPanel(
                this::continueGame,
                this::startNewGame,
                this::loadGame,
                this::openSettings,
                this::exitGame,
                hasSaves,
                settings // обязательно передаём настройки!
        ));
        revalidate();
        repaint();
    }

    public void startNewGame() {
        currentWormState = new WormState(100, 100, 0);
        currentEventMap = new EventMapModel();
        currentWormStats = new WormStats(0);

        WormStatsManager statsManager = new WormStatsManager(currentWormStats);

        currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager, settings); // передаем настройки
        currentMapPanel.setDesktopPane(desktopPane);
        currentMapPanel.setOnExitToMenu(this::showMainMenu);

        showMapPanel();
    }

    public void continueGame() {
        if (currentWormState != null && currentWormStats != null) {
            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager, settings);
            currentMapPanel.setDesktopPane(desktopPane);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);

            showMapPanel();
        } else {
            List<String> saves = WormSaveManager.listSaves();
            if (saves.isEmpty()) {
                JOptionPane.showMessageDialog(this, messages.getString("pause.no.saves"));
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

            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, this, statsManager, settings);
            currentMapPanel.setDesktopPane(desktopPane);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);

            WormSaveManager.load(
                    currentWormState,
                    currentWormStats,
                    currentMapPanel::setTarget,
                    saveName
            );

            showMapPanel();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    messages.getString("error.load.save") + ": " + e.getMessage());
        }
    }

    private void showMapPanel() {
        desktopPane.removeAll();
        currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
        desktopPane.add(currentMapPanel, JLayeredPane.DEFAULT_LAYER);
        setContentPane(desktopPane);

        revalidate();
        repaint();
        currentMapPanel.requestFocusInWindow();
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