package com.wormfarm.gui.dialog;

import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.SettingsDialog;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class PauseMenuDialog extends BaseInternalFrame {
    private JButton btnResume;
    private JButton btnLoad;
    private JButton btnSave;
    private JButton btnSettings;
    private JButton btnExitToMenu;
    private JButton btnExitToDesktop;
    private final Runnable onResumeCallback;
    private final UserSettings settings;
    private final Runnable onLanguageChanged;

    public PauseMenuDialog(
            JFrame owner,
            Runnable onResume,
            Runnable onExitToMenu,
            Runnable onExitToDesktop,
            Runnable onSave,
            Runnable onLoad,
            boolean loadEnabled,
            Runnable onLanguageChanged,
            UserSettings settings
    ) {
        super("pause.menu.title", false, true, false, false);

        this.onResumeCallback = onResume;
        this.onLanguageChanged = onLanguageChanged;
        this.settings = settings;

        // Убираем рамку и заголовок, запрещаем перемещение
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setUI(new javax.swing.plaf.basic.BasicInternalFrameUI(this) {
            @Override
            protected void installComponents() {
                // Не добавляем northPane (заголовок отсутствует)
            }
            @Override
            protected void installListeners() {
                // Не добавляем слушателей перемещения
            }
        });

        JPanel content = new JPanel(new GridBagLayout());

        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        btnResume = new JButton(messages.getString("pause.resume"));
        btnResume.setPreferredSize(new Dimension(220, 36));
        btnResume.addActionListener(e -> {
            dispose();
            if (onResumeCallback != null) onResumeCallback.run();
        });
        content.add(btnResume, gbc);

        gbc.gridy++;
        btnLoad = new JButton(messages.getString("pause.load"));
        btnLoad.setPreferredSize(new Dimension(220, 36));
        btnLoad.setEnabled(loadEnabled);
        btnLoad.addActionListener(e -> {
            if (onLoad != null) onLoad.run();
        });
        content.add(btnLoad, gbc);

        gbc.gridy++;
        btnSave = new JButton(messages.getString("pause.save"));
        btnSave.setPreferredSize(new Dimension(220, 36));
        btnSave.addActionListener(e -> {
            if (onSave != null) onSave.run();
        });
        content.add(btnSave, gbc);

        gbc.gridy++;
        btnSettings = new JButton(messages.getString("pause.settings"));
        btnSettings.setPreferredSize(new Dimension(220, 36));
        btnSettings.addActionListener(e -> {
            SettingsDialog settingsDialog = new SettingsDialog(owner, () -> {
                updateTexts();
                if (this.onLanguageChanged != null) this.onLanguageChanged.run();
            }, settings);
            settingsDialog.setLocationRelativeTo(this);
            settingsDialog.setVisible(true);
        });
        content.add(btnSettings, gbc);

        gbc.gridy++;
        btnExitToMenu = new JButton(messages.getString("pause.exit.menu"));
        btnExitToMenu.setPreferredSize(new Dimension(220, 36));
        btnExitToMenu.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });
        content.add(btnExitToMenu, gbc);

        gbc.gridy++;
        btnExitToDesktop = new JButton(messages.getString("pause.exit"));
        btnExitToDesktop.setPreferredSize(new Dimension(220, 36));
        btnExitToDesktop.addActionListener(e -> {
            if (onExitToDesktop != null) onExitToDesktop.run();
        });
        content.add(btnExitToDesktop, gbc);

        setContentPane(content);

        setSize(260, 380);
        setPreferredSize(new Dimension(260, 380));
        setClosable(true);
        setResizable(false);
        setIconifiable(false);
        setMaximizable(false);

        // ESC закрывает окно и возобновляет игру
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closePauseMenu");
        getRootPane().getActionMap().put("closePauseMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (onResumeCallback != null) onResumeCallback.run();
            }
        });

        updateLocale();
    }

    @Override
    protected String getTitleKey() {
        return "pause.menu.title";
    }

    @Override
    protected void updateComponents() {
        updateTexts();
    }

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