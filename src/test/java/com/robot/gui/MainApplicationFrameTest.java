package com.robot.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class MainApplicationFrameTest {
    private MainApplicationFrame mainApplicationFrame;

    @BeforeEach
    public void setUp() {
        mainApplicationFrame = new MainApplicationFrame();
        mainApplicationFrame.setVisible(true);
    }

    @Test
    public void testInitialSetup() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int inset = 50;
        assertEquals(new Rectangle(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2), mainApplicationFrame.getBounds());
        assertNotNull(mainApplicationFrame.getContentPane());
        assertTrue(mainApplicationFrame.getContentPane() instanceof JDesktopPane);
    }

    @Test
    public void testLogWindowCreation() {
        LogWindow logWindow = mainApplicationFrame.createLogWindow();
        mainApplicationFrame.addWindow(logWindow);
        assertEquals(new Dimension(300, 800), logWindow.getSize());
        assertEquals(new Point(10, 10), logWindow.getLocation());
        assertTrue(logWindow.isVisible());
    }

    @Test
    public void testGameWindowCreation() {
        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        mainApplicationFrame.addWindow(gameWindow);
        assertTrue(gameWindow.isVisible());
        assertEquals(new Dimension(400, 400), gameWindow.getSize());
    }

    @Test
    public void testLookAndFeelMenu() {
        JMenuBar menuBar = mainApplicationFrame.getJMenuBar();
        assertNotNull(menuBar);
        JMenu lookAndFeelMenu = menuBar.getMenu(0);
        assertEquals("Режим отображения", lookAndFeelMenu.getText());
    }

    @Test
    public void testTestMenu() {
        JMenuBar menuBar = mainApplicationFrame.getJMenuBar();
        assertNotNull(menuBar);
        JMenu testMenu = menuBar.getMenu(1);
        assertEquals("Тесты", testMenu.getText());
    }

    @Test
    public void testAddLogMessageMenuItem() {
        JMenuBar menuBar = mainApplicationFrame.getJMenuBar();
        JMenu testMenu = menuBar.getMenu(1);
        JMenuItem addLogMessageItem = testMenu.getItem(0);
        assertEquals("Сообщение в лог", addLogMessageItem.getText());
    }

    @Test
    public void testLookAndFeelChange() {
        SwingUtilities.invokeLater(() -> {
            JMenuBar menuBar = mainApplicationFrame.getJMenuBar();
            JMenu lookAndFeelMenu = menuBar.getMenu(0);
            JMenuItem systemLookAndFeel = lookAndFeelMenu.getItem(0);
            systemLookAndFeel.doClick();
            assertEquals(UIManager.getSystemLookAndFeelClassName(), UIManager.getLookAndFeel().getClass().getName());

            JMenuItem crossplatformLookAndFeel = lookAndFeelMenu.getItem(1);
            crossplatformLookAndFeel.doClick();
            assertEquals(UIManager.getCrossPlatformLookAndFeelClassName(), UIManager.getLookAndFeel().getClass().getName());
        });
    }
}