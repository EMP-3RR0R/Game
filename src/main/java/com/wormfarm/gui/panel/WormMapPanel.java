package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.logic.WormStatsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class WormMapPanel extends JPanel {
    public static final int FIELD_WIDTH = 800 - 16;
    public static final int FIELD_HEIGHT = 800 - 43;

    private final WormState worm;
    private final EventMapModel mapModel;
    private final JFrame ownerFrame;
    private final WormStatsManager statsManager;

    private JDesktopPane desktopPane;

    private volatile int targetX = 150;
    private volatile int targetY = 100;
    private volatile boolean paused = false;

    private final Set<EventMarker> activeMarkers = new HashSet<>();
    private final Map<EventMarker, Long> recentlyActivated = new HashMap<>();

    private final Timer timerRedraw;
    private final Timer timerModel;

    private Runnable onExitToMenu = null;
    public void setOnExitToMenu(Runnable onExitToMenu) {
        this.onExitToMenu = onExitToMenu;
    }

    private Runnable onLanguageChanged = null;
    public void setOnLanguageChanged(Runnable onLanguageChanged) {
        this.onLanguageChanged = onLanguageChanged;
    }

    private final WormMapEventManager eventManager;
    private final WormMapMenuHelper menuHelper;

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame, WormStatsManager statsManager) {
        this.worm = worm;
        this.mapModel = mapModel;
        this.ownerFrame = ownerFrame;
        this.statsManager = statsManager;

        this.eventManager = new WormMapEventManager(this, worm, mapModel, activeMarkers, recentlyActivated);
        this.menuHelper = new WormMapMenuHelper(this, worm, statsManager);

        setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        setFocusable(true);
        setLayout(null);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (paused) return;
                int dx = 0, dy = 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        dx = -10; break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        dx = 10; break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        dy = -10; break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        dy = 10; break;
                }
                if (dx != 0 || dy != 0) {
                    targetX = Math.max(0, Math.min(FIELD_WIDTH, targetX + dx));
                    targetY = Math.max(0, Math.min(FIELD_HEIGHT, targetY + dy));
                    repaint();
                }
            }
        });

        timerRedraw = new Timer(50, e -> {
            if (!paused) onRedrawEvent();
        });
        timerRedraw.start();

        timerModel = new Timer(10, e -> {
            if (!paused) onModelUpdateEvent();
        });
        timerModel.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (paused) return;
                setTargetPosition(e.getPoint());
                requestFocusInWindow();
            }
        });

        setDoubleBuffered(true);
        mapModel.addMarker(new EventMarker(200, 200, "Пятнашки"));

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "showPauseMenu");
        getActionMap().put("showPauseMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPauseMenu();
            }
        });

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame) {
        this(worm, mapModel, ownerFrame, null);
    }

    public void dispose() {
        timerRedraw.stop();
        timerModel.stop();
    }

    protected void setTargetPosition(Point p) {
        targetX = Math.max(0, Math.min(FIELD_WIDTH, p.x));
        targetY = Math.max(0, Math.min(FIELD_HEIGHT, p.y));
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        eventManager.updateWormAndEvents(targetX, targetY, paused);
    }

    void tryActivateEvent(EventMarker marker) {
        paused = true;
        int result = JOptionPane.showConfirmDialog(
                this,
                "Хотите начать испытание \"" + marker.getDescription() + "\"?",
                "Испытание",
                JOptionPane.YES_NO_OPTION
        );
        paused = false;
        requestFocusInWindow();
        if (result == JOptionPane.YES_OPTION) {
            eventManager.activateEvent(marker, ownerFrame, statsManager, () -> {
                paused = false;
                requestFocusInWindow();
            });
        }
    }

    public void setPaused(boolean value) {
        this.paused = value;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        WormMapRenderer.paintWholeMap(this, g, worm, mapModel, targetX, targetY, statsManager);
    }

    public void pauseGame() {
        setPaused(true);
        timerRedraw.stop();
        timerModel.stop();
    }
    public void resumeGame() {
        setPaused(false);
        timerRedraw.start();
        timerModel.start();
        requestFocusInWindow();
    }
    private void showPauseMenu() {
        pauseGame();
        menuHelper.showPauseMenu(ownerFrame, this::resumeGame, onExitToMenu, onLanguageChanged);
    }

    public int getTargetX() { return targetX; }

    public int getTargetY() { return targetY; }

    public void setTarget(int x, int y) {
        targetX = Math.max(0, Math.min(FIELD_WIDTH, x));
        targetY = Math.max(0, Math.min(FIELD_HEIGHT, y));
        repaint();
    }

    public void setDesktopPane(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }
}