/*package com.wormfarm.legacy;

 java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

import com.wormfarm.legacy.gui.BaseInternalFrame;
import com.wormfarm.legacy.gui.MainApplicationFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainApplicationFrameTest {

    private MainApplicationFrame frame;

    @BeforeEach
    public void setup() {
        BaseInternalFrame.setAppLocale(new Locale("en", "EN"));
        frame = new MainApplicationFrame();
    }

    @Test
    public void testResourceBundleLoading() {
        ResourceBundle messages = ResourceBundle.getBundle("com.robot.gui.messages", new Locale("en", "EN"));
        assertEquals("Exit", messages.getString("menu.exit"));
        assertEquals("Language", messages.getString("menu.language"));
    }

    @Test
    public void testChangeLanguageToRussian() {
        frame.changeLanguage(new Locale("ru", "RU"));
        assertEquals("Выход", frame.messages.getString("menu.exit"));
        assertEquals("Язык", frame.messages.getString("menu.language"));
    }

    @Test
    public void testMenuBarContainsExpectedItems() {
        JMenuBar menuBar = frame.getJMenuBar();
        assertNotNull(menuBar, "Menu bar should not be null");

        boolean hasLanguageMenu = false;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null && menu.getText().equals("Language")) {
                hasLanguageMenu = true;
            }
        }

        assertTrue(hasLanguageMenu, "Menu bar should contain 'Language' menu");
    }
}*/