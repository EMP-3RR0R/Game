package com.wormfarm.settings;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsDialog extends JDialog {
    private final Runnable onLanguageChange;
    private final JComboBox<String> langCombo;

    private final Map<String, String> langMap = new LinkedHashMap<>() {{
        put("English", "en");
        put("Русский", "ru");
    }};

    public SettingsDialog(Window owner, Runnable onLanguageChange) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.onLanguageChange = onLanguageChange;

        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        setTitle(messages.getString("settings.dialog.title"));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel labelLang = new JLabel(messages.getString("settings.language"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(labelLang, gbc);

        langCombo = new JComboBox<>(langMap.keySet().toArray(new String[0]));
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(langCombo, gbc);

        String current = AppLocale.getLocale().getLanguage();
        int idx = 0;
        for (String code : langMap.values()) {
            if (code.equals(current)) {
                langCombo.setSelectedIndex(idx);
                break;
            }
            idx++;
        }

        // Кнопки "Сохранить" и "Отмена"
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton(messages.getString("settings.save"));
        JButton btnCancel = new JButton(messages.getString("settings.cancel"));
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnPanel, gbc);

        btnSave.addActionListener(e -> {
            String selectedLang = langMap.get((String) langCombo.getSelectedItem());
            AppLocale.setLocale(selectedLang);
            dispose();
            if (onLanguageChange != null) onLanguageChange.run();
        });
        btnCancel.addActionListener(e -> dispose());

        setUndecorated(true);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}