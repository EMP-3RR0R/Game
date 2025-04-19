package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.logic.WormMover;
import com.wormfarm.minigames.fifteenpuzzle.ui.swing.FifteenPuzzleFrame;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WormMapPanel extends JPanel {
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 800;

    private final WormState worm;
    private final EventMapModel mapModel;
    private final JFrame ownerFrame;
    private volatile int targetX = 150;
    private volatile int targetY = 100;

    private final Set<EventMarker> activeMarkers = new HashSet<>();
    private final Map<EventMarker, Long> recentlyActivated = new HashMap<>();
    private volatile boolean paused = false;

    private final Timer timerRedraw;
    private final Timer timerModel;

    public WormMapPanel(WormState worm, EventMapModel mapModel, JFrame ownerFrame) {
        this.worm = worm;
        this.mapModel = mapModel;
        this.ownerFrame = ownerFrame;

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
        mapModel.addMarker(new EventMarker(300, 300, "Событие 2"));

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void tryActivateEvent(EventMarker marker) {
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
            activateEvent(marker);
        }
    }

    private void activateEvent(EventMarker marker) {
        if (marker.getDescription().equals("Пятнашки")) {
            paused = true;
            SwingUtilities.invokeLater(() -> {
                FifteenPuzzleFrame puzzleFrame = new FifteenPuzzleFrame();
                JDialog dialog = new JDialog(ownerFrame, "Пятнашки", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setContentPane(puzzleFrame.getContentPane());
                dialog.setSize(puzzleFrame.getPreferredSize());
                dialog.setLocationRelativeTo(ownerFrame);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        paused = false;
                        requestFocusInWindow();
                    }
                });
                dialog.setVisible(true);
            });
        }
        // Можно добавить обработку других событий
    }

    protected void setTargetPosition(Point p) {
        targetX = Math.max(0, Math.min(FIELD_WIDTH, p.x));
        targetY = Math.max(0, Math.min(FIELD_HEIGHT, p.y));
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        WormMover.moveWorm(worm, targetX, targetY, 10, FIELD_WIDTH, FIELD_HEIGHT);

        long now = System.currentTimeMillis();

        for (EventMarker marker : mapModel.getMarkers()) {
            double dist = WormMover.distance(worm.getX(), worm.getY(), marker.getX(), marker.getY());
            boolean inside = dist < 20;

            if (inside) {
                if (!activeMarkers.contains(marker) &&
                        (!recentlyActivated.containsKey(marker) ||
                                now - recentlyActivated.get(marker) > 5000)) {
                    activeMarkers.add(marker);
                    SwingUtilities.invokeLater(() -> tryActivateEvent(marker));
                }
            } else {
                if (activeMarkers.contains(marker)) {
                    activeMarkers.remove(marker);
                    recentlyActivated.put(marker, now);
                }
            }

            recentlyActivated.entrySet().removeIf(e -> now - e.getValue() > 5000 && !activeMarkers.contains(e.getKey()));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(new Color(120, 120, 120));
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        drawWorm(g2d, (int)worm.getX(), (int)worm.getY(), worm.getDirection());
        drawTarget(g2d, targetX, targetY);

        for (EventMarker marker : mapModel.getMarkers()) {
            drawMarker(g2d, marker);
        }

        g2d.dispose();
    }

    private void drawWorm(Graphics2D g, int x, int y, double direction) {
        AffineTransform oldTransform = g.getTransform();
        g.setTransform(AffineTransform.getRotateInstance(direction, x, y));
        g.setColor(Color.MAGENTA);
        g.fillOval(x - 15, y - 5, 30, 10);
        g.setColor(Color.BLACK);
        g.drawOval(x - 15, y - 5, 30, 10);
        g.setTransform(oldTransform);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.GREEN);
        g.fillOval(x - 5, y - 5, 10, 10);
    }

    private void drawMarker(Graphics2D g, EventMarker marker) {
        g.setColor(Color.RED);
        g.fillRect(marker.getX() - 10, marker.getY() - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(marker.getX() - 10, marker.getY() - 10, 20, 20);
    }
}