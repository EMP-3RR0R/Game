package com.wormfarm.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PauseMenuDialog extends JDialog {
    public PauseMenuDialog(
            JFrame owner,
            Runnable onResume,
            Runnable onExitToMenu,
            Runnable onExitToDesktop,
            Runnable onSave,
            Runnable onLoad,
            boolean loadEnabled
    ) {
        super(owner, "Меню", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.insets = new Insets(10,0,10,0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnResume = new JButton("Продолжить");
        btnResume.setPreferredSize(new Dimension(220, 36));
        btnResume.addActionListener(e -> {
            dispose();
            if (onResume != null) onResume.run();
        });
        add(btnResume, gbc);

        gbc.gridy++;
        JButton btnLoad = new JButton("Загрузить");
        btnLoad.setPreferredSize(new Dimension(220, 36));
        btnLoad.setEnabled(loadEnabled);
        btnLoad.addActionListener(e -> {
            dispose();
            if (onLoad != null) onLoad.run();
        });
        add(btnLoad, gbc);

        gbc.gridy++;
        JButton btnSave = new JButton("Сохранить");
        btnSave.setPreferredSize(new Dimension(220, 36));
        btnSave.addActionListener(e -> {
            dispose();
            if (onSave != null) onSave.run();
        });
        add(btnSave, gbc);

        gbc.gridy++;
        JButton btnSettings = new JButton("Настройки");
        btnSettings.setPreferredSize(new Dimension(220, 36));
        btnSettings.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Настройки пока недоступны.", "Заглушка", JOptionPane.INFORMATION_MESSAGE);
        });
        add(btnSettings, gbc);


        gbc.gridy++;
        JButton btnExitToMenu = new JButton("Выход в меню");
        btnExitToMenu.setPreferredSize(new Dimension(220, 36));
        btnExitToMenu.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });
        add(btnExitToMenu, gbc);

        gbc.gridy++;
        JButton btnExitToDesktop = new JButton("Выход на рабочий стол");
        btnExitToDesktop.setPreferredSize(new Dimension(220, 36));
        btnExitToDesktop.addActionListener(e -> {
            if (onExitToDesktop != null) onExitToDesktop.run();
        });
        add(btnExitToDesktop, gbc);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }
}