package com.robot.gui;

import java.awt.BorderLayout;
import javax.swing.JInternalFrame;

public class GameWindow extends JInternalFrame
{
    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        GameVisualizer visualizer = new GameVisualizer();
        getContentPane().add(visualizer, BorderLayout.CENTER);
        setSize(400, 400);
    }
}
