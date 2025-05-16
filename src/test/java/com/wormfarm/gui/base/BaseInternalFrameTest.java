package com.wormfarm.gui.base;

import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class BaseInternalFrameTest {
    private JFrame frame;
    private JDesktopPane desktop;

    @BeforeEach
    void setUp() {
        frame = new JFrame();
        desktop = new JDesktopPane();
        frame.setContentPane(desktop);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    @AfterEach
    void tearDown() {
        frame.dispose();
    }

    @Test
    void testRestoreNormalState() {
        TestInternalFrame win = new TestInternalFrame("test1");
        desktop.add(win);
        win.setBounds(120, 130, 350, 220);
        win.setVisible(true);

        InternalFrameState state = win.exportState();
        win.setBounds(10, 10, 100, 100);

        win.importState(state);
        assertEquals(120, win.getX());
        assertEquals(130, win.getY());
        assertEquals(350, win.getWidth());
        assertEquals(220, win.getHeight());
        assertFalse(win.isMaximum());
        assertFalse(win.isIcon());
    }

    @Test
    void testRestoreMaximizedState() throws Exception {
        TestInternalFrame win = new TestInternalFrame("test2");
        desktop.add(win);
        win.setBounds(150, 180, 400, 250);
        win.setVisible(true);

        win.setMaximum(true);
        InternalFrameState state = win.exportState();

        win.setMaximum(false);
        win.setBounds(10, 10, 200, 200);

        win.importState(state);
        assertTrue(win.isMaximum());

        win.setMaximum(false);
        assertEquals(150, win.getX());
        assertEquals(180, win.getY());
        assertEquals(400, win.getWidth());
        assertEquals(250, win.getHeight());
    }

    @Test
    void testRestoreIconifiedState() throws Exception {
        TestInternalFrame win = new TestInternalFrame("test3");
        desktop.add(win);
        win.setBounds(200, 210, 420, 180);
        win.setVisible(true);

        win.setIcon(true);
        InternalFrameState state = win.exportState();

        win.setIcon(false);
        win.setBounds(5, 5, 50, 50);

        win.importState(state);
        assertTrue(win.isIcon());
        win.setIcon(false);
        assertEquals(200, win.getX());
        assertEquals(210, win.getY());
        assertEquals(420, win.getWidth());
        assertEquals(180, win.getHeight());
    }

    @Test
    void testRestoreMaximizedThenIconified() throws Exception {
        TestInternalFrame win = new TestInternalFrame("test4");
        desktop.add(win);
        win.setBounds(350, 310, 300, 200);
        win.setVisible(true);

        win.setMaximum(true);
        win.setIcon(true);
        InternalFrameState state = win.exportState();

        win.setIcon(false);
        win.setMaximum(false);
        win.setBounds(10, 10, 50, 50);

        win.importState(state);
        assertTrue(win.isIcon());
        win.setIcon(false);
        assertTrue(win.isMaximum());
        win.setMaximum(false);

        assertEquals(350, win.getX());
        assertEquals(310, win.getY());
        assertEquals(300, win.getWidth());
        assertEquals(200, win.getHeight());
    }
}