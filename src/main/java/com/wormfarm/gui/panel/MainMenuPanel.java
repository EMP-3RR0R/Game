package com.wormfarm.gui.panel;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(Runnable onContinue, Runnable onNewGame, Runnable onLoadGame, Runnable onSettings, Runnable onExit) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JButton btnContinue = new JButton("Продолжить");
        btnContinue.setPreferredSize(new Dimension(200, 40));
        btnContinue.addActionListener(e -> onContinue.run());
        add(btnContinue, gbc);

        gbc.gridy++;
        JButton btnNewGame = new JButton("Новая игра");
        btnNewGame.setPreferredSize(new Dimension(200, 40));
        btnNewGame.addActionListener(e -> onNewGame.run());
        add(btnNewGame, gbc);

        gbc.gridy++;
        JButton btnLoadGame = new JButton("Загрузить");
        btnLoadGame.setPreferredSize(new Dimension(200, 40));
        btnLoadGame.addActionListener(e -> onLoadGame.run());
        add(btnLoadGame, gbc);

        gbc.gridy++;
        JButton btnSettings = new JButton("Настройки");
        btnSettings.setPreferredSize(new Dimension(200, 40));
        btnSettings.addActionListener(e -> onSettings.run());
        add(btnSettings, gbc);

        gbc.gridy++;
        JButton btnExit = new JButton("Выход");
        btnExit.setPreferredSize(new Dimension(200, 40));
        btnExit.addActionListener(e -> onExit.run());
        add(btnExit, gbc);
    }
}
