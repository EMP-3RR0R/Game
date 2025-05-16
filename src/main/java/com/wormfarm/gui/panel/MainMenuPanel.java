package com.wormfarm.gui.panel;

import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.SettingsDialog;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class MainMenuPanel extends JPanel {

    private JDialog settingsDialog;
    private final Runnable onSettings; // <-- final
    private final UserSettings settings;

    public MainMenuPanel(
            Runnable onContinue,
            Runnable onNewGame,
            Runnable onLoadGame,
            Runnable onSettings, // <-- сюда передаём mainFrame::updateLocale
            Runnable onExit,
            boolean hasSaves,
            UserSettings settings
    ) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());

        JButton btnContinue = new JButton(messages.getString("main.menu.continue"));
        btnContinue.setPreferredSize(new Dimension(200, 40));
        btnContinue.addActionListener(e -> onContinue.run());
        btnContinue.setEnabled(hasSaves);
        add(btnContinue, gbc);

        gbc.gridy++;
        JButton btnNewGame = new JButton(messages.getString("main.menu.newgame"));
        btnNewGame.setPreferredSize(new Dimension(200, 40));
        btnNewGame.addActionListener(e -> onNewGame.run());
        add(btnNewGame, gbc);

        gbc.gridy++;
        JButton btnLoadGame = new JButton(messages.getString("main.menu.loadgame"));
        btnLoadGame.setPreferredSize(new Dimension(200, 40));
        btnLoadGame.addActionListener(e -> onLoadGame.run());
        btnLoadGame.setEnabled(hasSaves);
        add(btnLoadGame, gbc);

        gbc.gridy++;
        JButton btnSettings = new JButton(messages.getString("main.menu.settings"));
        btnSettings.setPreferredSize(new Dimension(200, 40));
        btnSettings.addActionListener(e -> showSettingsDialog());
        add(btnSettings, gbc);

        gbc.gridy++;
        JButton btnExit = new JButton(messages.getString("main.menu.exit"));
        btnExit.setPreferredSize(new Dimension(200, 40));
        btnExit.addActionListener(e -> onExit.run());
        add(btnExit, gbc);

        this.settingsDialog = null;
        this.onSettings = onSettings;
        this.settings = settings;
    }

    private void showSettingsDialog() {
        if (settingsDialog != null && settingsDialog.isShowing()) {
            settingsDialog.toFront();
            return;
        }
        // Важно: вызывать глобальный колбэк!
        settingsDialog = new SettingsDialog(
                SwingUtilities.getWindowAncestor(this),
                () -> {
                    if (onSettings != null) onSettings.run(); // <-- теперь только это!
                    updateTexts(); // Можно оставить, чтобы кнопки тоже обновились мгновенно
                },
                settings
        );
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
    }

    public void updateTexts() {
        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        ((JButton)getComponent(0)).setText(messages.getString("main.menu.continue"));
        ((JButton)getComponent(1)).setText(messages.getString("main.menu.newgame"));
        ((JButton)getComponent(2)).setText(messages.getString("main.menu.loadgame"));
        ((JButton)getComponent(3)).setText(messages.getString("main.menu.settings"));
        ((JButton)getComponent(4)).setText(messages.getString("main.menu.exit"));
    }
}