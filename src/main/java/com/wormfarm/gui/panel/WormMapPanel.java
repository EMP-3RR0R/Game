package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.logic.WormMover;
import com.wormfarm.gui.frame.MainApplicationFrame;
import com.wormfarm.minigames.fifteenpuzzle.ui.swing.FifteenPuzzleFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Image;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WormMapPanel extends JPanel {
    public static final int FIELD_WIDTH = MainApplicationFrame.MAP_HEIGHT-16;
    public static final int FIELD_HEIGHT = MainApplicationFrame.MAP_HEIGHT-43;

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

    // --- Текстура земли для фона ---
    private static BufferedImage groundTexture = null;
    private static final int GROUND_TEXTURE_SIZE = 64;

    // --- Иконка пятнашек для маркера ---
    private static BufferedImage fifteenPuzzleIcon = null;

    static {
        // Земля
        try (InputStream in = WormMapPanel.class.getResourceAsStream("/images/ground.png")) {
            if (in != null) {
                BufferedImage orig = ImageIO.read(in);
                if (orig != null) {
                    groundTexture = resizeTexture(orig, GROUND_TEXTURE_SIZE, GROUND_TEXTURE_SIZE);
                }
            }
        } catch (Exception e) {
            groundTexture = null;
        }
        // Иконка пятнашек
        try (InputStream in = WormMapPanel.class.getResourceAsStream("/images/FifteenPuzzle/FifteenPuzzleIcon.png")) {
            if (in != null) {
                fifteenPuzzleIcon = ImageIO.read(in);
            }
        } catch (Exception e) {
            fifteenPuzzleIcon = null;
        }
    }

    private static BufferedImage resizeTexture(BufferedImage img, int targetW, int targetH) {
        Image scaled = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        BufferedImage small = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = small.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return small;
    }

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

        // Только пятнашки!
        mapModel.addMarker(new EventMarker(200, 200, "Пятнашки"));
        // mapModel.addMarker(new EventMarker(300, 300, "Событие 2")); // убрал второй ивент

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
                puzzleFrame.setParentDialog(dialog);

                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // <--- Важно!
                dialog.setContentPane(puzzleFrame.getContentPane());
                dialog.setSize(puzzleFrame.getPreferredSize());
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(ownerFrame);

                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(
                                dialog,
                                "Вы уверены, что хотите выйти из пятнашек?\nПрогресс будет потерян.",
                                "Подтвердите выход",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            dialog.dispose();
                        }
                        // Если NO — ничего не делаем, окно не закроется
                    }
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
            boolean inside = dist < 40;

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

        // Фон из земли (тайл)
        if (groundTexture != null) {
            int texW = groundTexture.getWidth();
            int texH = groundTexture.getHeight();
            for (int y = 0; y < FIELD_HEIGHT; y += texH) {
                for (int x = 0; x < FIELD_WIDTH; x += texW) {
                    g2d.drawImage(groundTexture, x, y, this);
                }
            }
        } else {
            g2d.setColor(new Color(120, 120, 120));
            g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        }

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        drawWorm(g2d, (int)worm.getX(), (int)worm.getY(), worm.getDirection());
        drawTarget(g2d, targetX, targetY);

        for (EventMarker marker : mapModel.getMarkers()) {
            if ("Пятнашки".equals(marker.getDescription())) {
                drawFifteenPuzzleIcon(g2d, marker);
            } else {
                drawMarker(g2d, marker);
            }
        }

        g2d.dispose();
    }

    private void drawFifteenPuzzleIcon(Graphics2D g, EventMarker marker) {
        // Большая прозрачная зона действия
        int zoneRadius = 40; // радиус зоны действия
        g.setColor(new Color(100, 200, 255, 60));
        int zoneX = marker.getX() - zoneRadius;
        int zoneY = marker.getY() - zoneRadius;
        g.fillOval(zoneX, zoneY, zoneRadius * 2, zoneRadius * 2);

        if (fifteenPuzzleIcon == null) {
            drawMarker(g, marker);
            return;
        }
        int size = 72; // крупная иконка
        int x = marker.getX() - size / 2 +2;
        int y = marker.getY() - size / 2 + 5;
        g.drawImage(fifteenPuzzleIcon, x, y, size, size, null);
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