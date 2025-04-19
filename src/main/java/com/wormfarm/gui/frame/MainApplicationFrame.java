package com.wormfarm.gui.frame;

import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMapModel;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.Locale;

public class MainApplicationFrame extends JFrame {
    public static final int MAP_WIDTH = 800;
    public static final int MAP_HEIGHT = 800;

    protected final JDesktopPane desktopPane = new JDesktopPane();
    protected ResourceBundle messages;

    public MainApplicationFrame() {
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", Locale.getDefault());

        setTitle("Worm Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Основная панель с червём и картой
        WormState worm = new WormState(100, 100, 0);
        EventMapModel eventMap = new EventMapModel();
        WormMapPanel mapPanel = new WormMapPanel(worm, eventMap, this); // Передаём ссылку на JFrame

        setContentPane(mapPanel);

        setJMenuBar(createMenuBar());
        setResizable(false);
        setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMinimumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setMaximumSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // Добавь пункты меню, если нужно
        return menuBar;
    }
}