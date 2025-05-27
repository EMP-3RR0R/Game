package com.wormfarm.settings;

import com.wormfarm.gui.dialog.DialogState;
import com.wormfarm.util.SoundUtils;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsDialog extends JDialog {
    private final Runnable onLanguageChange;
    private final JComboBox<String> langCombo;
    private final JSlider volumeSlider;
    private final Map<String, String> langMap = new LinkedHashMap<>() {{
        put("English", "en");
        put("Русский", "ru");
    }};
    private final UserSettings settings;

    public SettingsDialog(Window owner, Runnable onLanguageChange, UserSettings settings) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.onLanguageChange = onLanguageChange;
        this.settings = settings;

        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        setTitle(messages.getString("settings.dialog.title"));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel labelLang = new JLabel(messages.getString("settings.language"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(labelLang, gbc);

        langCombo = new JComboBox<>(langMap.keySet().toArray(new String[0]));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(langCombo, gbc);

        String currentLang = settings.getLanguage();
        int idx = 0;
        for (String code : langMap.values()) {
            if (code.equals(currentLang)) {
                langCombo.setSelectedIndex(idx);
                break;
            }
            idx++;
        }

        JLabel labelVolume = new JLabel(messages.getString("settings.volume"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(labelVolume, gbc);

        volumeSlider = new JSlider(0, 100, (int) (settings.getVolume() * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(volumeSlider, gbc);

        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton(messages.getString("settings.save"));
        JButton btnCancel = new JButton(messages.getString("settings.cancel"));
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnPanel, gbc);

        btnSave.addActionListener(e -> {
            String selectedLang = langMap.get((String) langCombo.getSelectedItem());
            settings.setLanguage(selectedLang);
            AppLocale.setLocale(selectedLang); // Меняем глобальную локаль приложения

            float volume = volumeSlider.getValue() / 100.0f;
            settings.setVolume(volume);
            SoundUtils.setVolume(volume);

            try {
                UserSettings.save(settings, new java.io.File("user.settings"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения настроек: " + ex.getMessage());
            }

            dispose();
            if (onLanguageChange != null) onLanguageChange.run();
        });
        btnCancel.addActionListener(e -> dispose());

        setUndecorated(true);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public String getDialogKey() {
        return "settings.dialog";
    }

    public DialogState exportState() {
        DialogState state = new DialogState(getDialogKey());
        state.x = getX();
        state.y = getY();
        state.width = getWidth();
        state.height = getHeight();
        state.visible = isVisible();
        String selectedLang = langMap.get((String) langCombo.getSelectedItem());
        int volume = volumeSlider.getValue();
        state.extra = selectedLang + ";" + volume;
        return state;
    }

    public void importState(DialogState state) {
        setLocation(state.x, state.y);
        setSize(state.width, state.height);
        setVisible(state.visible);
        if (state.extra != null) {
            String[] parts = state.extra.split(";");
            if (parts.length == 2) {
                String langCode = parts[0];
                try {
                    int idx = 0;
                    for (String code : langMap.values()) {
                        if (code.equals(langCode)) {
                            langCombo.setSelectedIndex(idx);
                            break;
                        }
                        idx++;
                    }
                } catch (Exception ignored) {}
                try {
                    int vol = Integer.parseInt(parts[1]);
                    volumeSlider.setValue(vol);
                } catch (Exception ignored) {}
            }
        }
    }
}