package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Locale;

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

    private Runnable onExitToDesktop = null;
    public void setOnExitToDesktop(Runnable onExitToDesktop) {
        this.onExitToDesktop = onExitToDesktop;
    }

    private Runnable onLanguageChanged = null;
    public void setOnLanguageChanged(Runnable onLanguageChanged) {
        this.onLanguageChanged = onLanguageChanged;
    }

    private final WormMapEventManager eventManager;
    private final WormMapMenuHelper menuHelper;

    private final UserSettings settings;
    private ResourceBundle messages;

    // Новый блок: для связи с GameSessionManager
    private GameSessionManager gameSessionManager;
    public void setGameSessionManager(GameSessionManager gsm) {
        this.gameSessionManager = gsm;
    }
    public GameSessionManager getGameSessionManager() {
        return gameSessionManager;
    }

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame, WormStatsManager statsManager, UserSettings settings) {
        this.worm = worm;
        this.mapModel = mapModel;
        this.ownerFrame = ownerFrame;
        this.statsManager = statsManager;
        this.settings = settings;

        this.messages = ResourceBundle.getBundle(
                "com.wormfarm.gui.messages",
                settings != null && settings.getLanguage() != null
                        ? new Locale(settings.getLanguage())
                        : Locale.getDefault()
        );

        this.eventManager = new WormMapEventManager(this, worm, mapModel, activeMarkers, recentlyActivated, messages);
        this.menuHelper = new WormMapMenuHelper(this, worm, statsManager, settings);

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
        mapModel.addMarker(new EventMarker(200, 200, "puzzle.title"));

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

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame, WormStatsManager statsManager) {
        this(worm, mapModel, ownerFrame, statsManager, null);
    }

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame) {
        this(worm, mapModel, ownerFrame, null, null);
    }

    public void shutdown() {
        timerRedraw.stop();
        timerModel.stop();
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
        }
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
        String eventTitle = messages.getString(marker.getDescription());
        String confirmMessage = MessageFormat.format(
                messages.getString("challenge.confirm.message"),
                eventTitle
        );
        int result = JOptionPane.showConfirmDialog(
                this,
                confirmMessage,
                messages.getString("challenge.confirm.title"),
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
        WormMapRenderer.paintWholeMap(this, g, worm, mapModel, targetX, targetY, statsManager, messages);
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

    public void updateLocale() {
        Locale locale = settings != null && settings.getLanguage() != null
                ? new Locale(settings.getLanguage())
                : Locale.getDefault();
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", locale);
        repaint();
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    private void showPauseMenu() {
        pauseGame();
        menuHelper.showPauseMenu(
                ownerFrame,
                this::resumeGame,
                onExitToMenu,
                onExitToDesktop,
                onLanguageChanged
        );
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
    public WormStatsManager getStatsManager() {
        return statsManager;
    }
}