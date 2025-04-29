package com.wormfarm.gui.dialog;

import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class PauseMenuDialog extends JDialog {
    private JButton btnResume;
    private JButton btnLoad;
    private JButton btnSave;
    private JButton btnSettings;
    private JButton btnExitToMenu;
    private JButton btnExitToDesktop;

    public PauseMenuDialog(
            JFrame owner,
            Runnable onResume,
            Runnable onExitToMenu,
            Runnable onExitToDesktop,
            Runnable onSave,
            Runnable onLoad,
            boolean loadEnabled,
            Runnable onLanguageChanged
    ) {
        super(owner, ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale()).getString("pause.menu.title"), true);

        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.insets = new Insets(10,0,10,0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        btnResume = new JButton(messages.getString("pause.resume"));
        btnResume.setPreferredSize(new Dimension(220, 36));
        btnResume.addActionListener(e -> {
            dispose();
            if (onResume != null) onResume.run();
        });
        add(btnResume, gbc);

        gbc.gridy++;
        btnLoad = new JButton(messages.getString("pause.load"));
        btnLoad.setPreferredSize(new Dimension(220, 36));
        btnLoad.setEnabled(loadEnabled);
        btnLoad.addActionListener(e -> {
            dispose();
            if (onLoad != null) onLoad.run();
        });
        add(btnLoad, gbc);

        gbc.gridy++;
        btnSave = new JButton(messages.getString("pause.save"));
        btnSave.setPreferredSize(new Dimension(220, 36));
        btnSave.addActionListener(e -> {
            dispose();
            if (onSave != null) onSave.run();
        });
        add(btnSave, gbc);

        gbc.gridy++;
        btnSettings = new JButton(messages.getString("pause.settings"));
        btnSettings.setPreferredSize(new Dimension(220, 36));
        btnSettings.addActionListener(e -> {
            SettingsDialog settingsDialog = new SettingsDialog(this, () -> {
                updateTexts();
                if (onLanguageChanged != null) onLanguageChanged.run();
            });
            settingsDialog.setLocationRelativeTo(this);
            settingsDialog.setVisible(true);
        });
        add(btnSettings, gbc);

        gbc.gridy++;
        btnExitToMenu = new JButton(messages.getString("pause.exit.menu"));
        btnExitToMenu.setPreferredSize(new Dimension(220, 36));
        btnExitToMenu.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });
        add(btnExitToMenu, gbc);

        gbc.gridy++;
        btnExitToDesktop = new JButton(messages.getString("pause.exit"));
        btnExitToDesktop.setPreferredSize(new Dimension(220, 36));
        btnExitToDesktop.addActionListener(e -> {
            if (onExitToDesktop != null) onExitToDesktop.run();
        });
        add(btnExitToDesktop, gbc);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // Обновление текстов после смены языка
    private void updateTexts() {
        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        setTitle(messages.getString("pause.menu.title"));
        btnResume.setText(messages.getString("pause.resume"));
        btnLoad.setText(messages.getString("pause.load"));
        btnSave.setText(messages.getString("pause.save"));
        btnSettings.setText(messages.getString("pause.settings"));
        btnExitToMenu.setText(messages.getString("pause.exit.menu"));
        btnExitToDesktop.setText(messages.getString("pause.exit"));
    }
}