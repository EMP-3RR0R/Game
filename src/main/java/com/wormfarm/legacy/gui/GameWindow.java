package com.wormfarm.legacy.gui;

import com.wormfarm.legacy.core.GameVisualizer;

import java.awt.*;

public class GameWindow extends BaseInternalFrame {
    public GameWindow() {
        super("game.window.title", true, true, true, true);
        GameVisualizer visualizer = new GameVisualizer();
        getContentPane().add(visualizer, BorderLayout.CENTER);
        setSize(400, 400);
    }

    @Override
    protected String getTitleKey() {
        return "game.window.title";
    }

    @Override
    protected void updateComponents() {
    }
}