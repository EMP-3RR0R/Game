package com.robot.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

import com.robot.log.Logger;
import com.robot.log.LogWindowSource;

public class MainApplicationFrame extends JFrame {
    protected final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;
    protected ResourceBundle messages;

    public MainApplicationFrame() {
        messages = ResourceBundle.getBundle("com.robot.gui.messages", BaseInternalFrame.currentLocale);
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
        setContentPane(desktopPane);

        logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    protected void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
                this,
                messages.getString("confirm.exit.message"),
                messages.getString("confirm.exit.title"),
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            LogWindowSource source = Logger.getDefaultLogSource();
            source.unregisterListener(logWindow);
            System.exit(0);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createLanguageMenu());
        menuBar.add(createTestMenu());

        JMenuItem exitItem = new JMenuItem(messages.getString("menu.exit"));
        exitItem.addActionListener(e -> exitApplication());
        menuBar.add(exitItem);

        return menuBar;
    }

    private JMenu createLanguageMenu() {
        JMenu languageMenu = new JMenu(messages.getString("menu.language"));
        languageMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem englishItem = new JMenuItem("English");
        englishItem.addActionListener(e -> changeLanguage(new Locale("en", "EN")));

        JMenuItem russianItem = new JMenuItem("Русский");
        russianItem.addActionListener(e -> changeLanguage(new Locale("ru", "RU")));

        languageMenu.add(englishItem);
        languageMenu.add(russianItem);
        return languageMenu;
    }

    protected void changeLanguage(Locale locale) {
        BaseInternalFrame.setAppLocale(locale);
        messages = ResourceBundle.getBundle("com.robot.gui.messages", locale);
        updateAllWindows();
        SwingUtilities.updateComponentTreeUI(this);
        invalidate();
        repaint();
    }

    protected void updateAllWindows() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof BaseInternalFrame) {
                ((BaseInternalFrame) frame).updateLocale();
            }
        }
    }
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.pack();
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }


    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription("Управление режимом отображения приложения");

        lookAndFeelMenu.add(createSystemLookAndFeelMenuItem());
        lookAndFeelMenu.add(createCrossplatformLookAndFeelMenuItem());

        return lookAndFeelMenu;
    }

    private JMenuItem createSystemLookAndFeelMenuItem() {
        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        return systemLookAndFeel;
    }

    private JMenuItem createCrossplatformLookAndFeelMenuItem() {
        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        return crossplatformLookAndFeel;
    }

    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        testMenu.add(createAddLogMessageMenuItem());

        return testMenu;
    }

    private JMenuItem createAddLogMessageMenuItem() {
        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        return addLogMessageItem;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // just ignore
        }
    }
}