package com.wormfarm.gui.frame;

import com.wormfarm.gui.state.AppStateRestorer;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.gui.state.WindowProfileManager;
import com.wormfarm.gui.state.DialogManager;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ResourceBundle;

public class MainApplicationFrame extends JFrame {
    public static final int MAP_WIDTH = 800;
    public static final int MAP_HEIGHT = 800;

    public final JDesktopPane desktopPane = new JDesktopPane();
    public ResourceBundle messages;

    public UserSettings settings;
    public final GameSessionManager gameSessionManager;
    public final WindowProfileManager windowProfileManager;
    public final DialogManager dialogManager;

    public MainApplicationFrame() {
        // --- Загрузка настроек пользователя ---
        try {
            File settingsFile = new File("user.settings");
            if (settingsFile.exists()) {
                settings = UserSettings.load(settingsFile);
            } else {
                settings = new UserSettings();
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

        AppLocale.setLocale(settings.getLanguage());
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());

        setTitle(messages.getString("app.title"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // --- Инициализация менеджеров ---
        this.gameSessionManager = new GameSessionManager(this, settings, messages, desktopPane);
        this.dialogManager = new DialogManager(this, messages, gameSessionManager);
        this.gameSessionManager.setDialogManager(dialogManager); // ВАЖНО!
        this.windowProfileManager = new WindowProfileManager(this, messages, desktopPane, gameSessionManager);

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
                    gameSessionManager.saveAppState();
                    windowProfileManager.saveWindowsProfile();
                    dialogManager.saveDialogStates();
                    dispose();
                    // ГАРАНТИРОВАННОЕ завершение процесса (решает проблему висящего процесса)
                    System.exit(0);
                }
            }
        });

        setJMenuBar(windowProfileManager.createMenuBarProfiles());
        setResizable(false);
        setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMinimumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMaximumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        pack();
        setLocationRelativeTo(null);

        desktopPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gameSessionManager.resizeCurrentMapPanel();
            }
        });

        gameSessionManager.showMainMenu();

        // Восстановление состояния приложения
        new AppStateRestorer(this, messages, gameSessionManager, windowProfileManager, dialogManager).tryRestoreAppState();
    }

}