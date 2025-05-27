package com.wormfarm.gui.dialog;

import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.SettingsDialog;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;

public class PauseMenuDialog extends BaseInternalFrame {
    JButton btnResume;
    JButton btnLoad;
    JButton btnSave;
    JButton btnSettings;
    JButton btnExitToMenu;
    JButton btnExitToDesktop;

    private final Runnable onResumeCallback;
    private final Runnable onExitToMenuCallback;
    private final Runnable onExitToDesktopCallback;
    private final UserSettings settings;
    private final Runnable onLanguageChanged;

    private final WormState worm;
    private final WormStatsManager statsManager;
    private final int targetX;
    private final int targetY;
    private final Component parentComponent;
    private final GameSessionManager gameSessionManager;

    public PauseMenuDialog(
            JFrame owner,
            Runnable onResume,
            Runnable onExitToMenu,
            Runnable onExitToDesktop,
            WormState worm,
            WormStatsManager statsManager,
            int targetX,
            int targetY,
            UserSettings settings,
            Runnable onLanguageChanged,
            Component parentComponent,
            GameSessionManager gameSessionManager
    ) {
        super("pause.menu.title", false, true, false, false);

        this.onResumeCallback = onResume;
        this.onExitToMenuCallback = onExitToMenu;
        this.onExitToDesktopCallback = onExitToDesktop;
        this.settings = settings;
        this.onLanguageChanged = onLanguageChanged;
        this.worm = worm;
        this.statsManager = statsManager;
        this.targetX = targetX;
        this.targetY = targetY;
        this.parentComponent = parentComponent;
        this.gameSessionManager = gameSessionManager;

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setUI(new javax.swing.plaf.basic.BasicInternalFrameUI(this) {
            @Override
            protected void installComponents() {}
            @Override
            protected void installListeners() {}
        });

        JPanel content = new JPanel(new GridBagLayout());
        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 20, 10, 20);
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
        btnLoad.setEnabled(!WormSaveManager.listSaves().isEmpty());
        btnLoad.addActionListener(e -> {
            List<String> saves = WormSaveManager.listSaves();
            LoadGameDialog dlg = new LoadGameDialog(getFrameAncestor(), saves);
            dlg.setLocationRelativeTo(parentComponent != null ? parentComponent : getFrameAncestor());
            dlg.setModal(true);
            dlg.setVisible(true);
            String saveName = dlg.getSelectedName();
            if (saveName != null) {
                try {
                    if (gameSessionManager != null) {
                        gameSessionManager.loadSpecificGame(saveName);
                        JOptionPane.showMessageDialog(parentComponent, "Игра '" + saveName + "' загружена!");
                    } else {
                        WormSaveManager.load(
                                worm,
                                statsManager.getStats(),
                                (x, y) -> {},
                                (farmSave, found) -> {},
                                saveName
                        );
                        JOptionPane.showMessageDialog(parentComponent, "Игра '" + saveName + "' загружена (только червяк)!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        content.add(btnLoad, gbc);

        gbc.gridy++;
        btnSave = new JButton(messages.getString("pause.save"));
        btnSave.setPreferredSize(new Dimension(220, 36));
        btnSave.addActionListener(e -> {
            List<String> saves = WormSaveManager.listSaves();
            SaveGameDialog dlg = new SaveGameDialog(getFrameAncestor(), saves);
            dlg.setLocationRelativeTo(parentComponent != null ? parentComponent : getFrameAncestor());
            dlg.setModal(true);
            dlg.setVisible(true);
            String saveName = dlg.getSelectedName();
            if (saveName != null) {
                try {
                    if (gameSessionManager != null) {
                        gameSessionManager.saveCurrentGame(saveName);
                        JOptionPane.showMessageDialog(parentComponent, "Игра сохранена как '" + saveName + "'!");
                    } else {
                        WormSaveManager.save(
                                worm,
                                statsManager.getStats(),
                                targetX,
                                targetY,
                                null,
                                saveName
                        );
                        JOptionPane.showMessageDialog(parentComponent, "Игра сохранена как '" + saveName + "' (только червяк)!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentComponent, "Ошибка сохранения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        content.add(btnSave, gbc);

        gbc.gridy++;
        btnSettings = new JButton(messages.getString("pause.settings"));
        btnSettings.setPreferredSize(new Dimension(220, 36));
        btnSettings.addActionListener(e -> {
            SettingsDialog settingsDialog = new SettingsDialog(getFrameAncestor(), () -> {
                if (this.onLanguageChanged != null) this.onLanguageChanged.run();
                updateTexts();
            }, settings);
            settingsDialog.setLocationRelativeTo(parentComponent != null ? parentComponent : getFrameAncestor());
            settingsDialog.setVisible(true);
        });
        content.add(btnSettings, gbc);

        gbc.gridy++;
        btnExitToMenu = new JButton(messages.getString("pause.exit.menu"));
        btnExitToMenu.setPreferredSize(new Dimension(220, 36));
        btnExitToMenu.addActionListener(e -> {
            dispose();
            if (onExitToMenuCallback != null) onExitToMenuCallback.run();
        });
        content.add(btnExitToMenu, gbc);

        gbc.gridy++;
        btnExitToDesktop = new JButton(messages.getString("pause.exit"));
        btnExitToDesktop.setPreferredSize(new Dimension(220, 36));
        btnExitToDesktop.addActionListener(e -> {
            dispose();
            if (onExitToDesktopCallback != null) onExitToDesktopCallback.run();
        });
        content.add(btnExitToDesktop, gbc);

        setContentPane(content);

        setSize(260, 380);
        setPreferredSize(new Dimension(260, 380));
        setClosable(true);
        setResizable(false);
        setIconifiable(false);
        setMaximizable(false);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closePauseMenu");
        getRootPane().getActionMap().put("closePauseMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (onResumeCallback != null) onResumeCallback.run();
            }
        });

        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                if (gameSessionManager != null) {
                    gameSessionManager.onResumableWindowClosed();
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) {
                        mapPanel.repaint();
                    }
                }
            }
        });

        updateLocale();
    }

    private JFrame getFrameAncestor() {
        if (parentComponent instanceof JFrame) {
            return (JFrame) parentComponent;
        }
        Window w = SwingUtilities.getWindowAncestor(parentComponent);
        return w instanceof JFrame ? (JFrame) w : null;
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

    @Override
    public String getWindowKey() {
        return "pause.menu";
    }
}