package com.robot.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class GameVisualizer extends JPanel {
    private final Timer m_timer = initTimer();
    private final List<EventMarker> eventMarkers = new ArrayList<>(); // Список событий
    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    public GameVisualizer() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 10);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });

        setDoubleBuffered(true);

        // Пример добавления событий на карту
        addEventMarker(200, 200, "Пятнашки");
        addEventMarker(300, 300, "Событие 2");
    }

    // Добавление события на карту
    public void addEventMarker(int x, int y, String description) {
        eventMarkers.add(new EventMarker(x, y, description));
        repaint();
    }

    private void handleMouseClick(Point clickPoint) {
        // Проверяем, кликнули ли в событие
        for (EventMarker marker : eventMarkers) {
            if (marker.contains(clickPoint)) {
                System.out.println("Активировано событие: " + marker.getDescription());
                if (marker.getDescription().equals("Пятнашки")) {
                    // Добавляем в JDesktopPane для отображения JInternalFrame
                    SwingUtilities.invokeLater(() -> {
                        JDesktopPane desktopPane = new JDesktopPane(); // Это должен быть внешний контейнер
                        FifteenPuzzleFrame puzzleFrame = new FifteenPuzzleFrame();
                        desktopPane.add(puzzleFrame);
                        puzzleFrame.setVisible(true);
                        JFrame frame = new JFrame("Puzzle Game");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setContentPane(desktopPane);
                        frame.setSize(800, 600);
                        frame.setVisible(true);
                    });
                } else {
                    activateEvent(marker);
                }
                return;
            }
        }
        // Если не попали в событие, устанавливаем новую цель для робота
        setTargetPosition(clickPoint);
    }

    // Логика активации события
    private void activateEvent(EventMarker marker) {
        System.out.println("Открытие окна для события: " + marker.getDescription());
        // Здесь может быть логика для других событий
    }

    protected void setTargetPosition(Point p) {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        double distance = distance(m_targetPositionX, m_targetPositionY, m_robotPositionX, m_robotPositionY);
        if (distance < 0.5) {
            return;
        }
        double velocity = maxVelocity;
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection) {
            angularVelocity = maxAngularVelocity;
        }
        if (angleToTarget < m_robotDirection) {
            angularVelocity = -maxAngularVelocity;
        }

        moveRobot(velocity, angularVelocity, 10);
    }

    static double distance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;
        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);
        double newX = m_robotPositionX + velocity / angularVelocity *
                (Math.sin(m_robotDirection + angularVelocity * duration) - Math.sin(m_robotDirection));
        if (!Double.isFinite(newX)) {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
                (Math.cos(m_robotDirection + angularVelocity * duration) - Math.cos(m_robotDirection));
        if (!Double.isFinite(newY)) {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }

        int fieldWidth = getWidth();
        int fieldHeight = getHeight();

        if (fieldWidth <= 0) fieldWidth = 400;
        if (fieldHeight <= 0) fieldHeight = 400;

        m_robotPositionX = (newX % fieldWidth + fieldWidth) % fieldWidth;
        m_robotPositionY = (newY % fieldHeight + fieldHeight) % fieldHeight;
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
        m_robotDirection = newDirection;
    }

    static double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Рисуем карту (фон)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Рисуем робота
        drawRobot(g2d, (int) m_robotPositionX, (int) m_robotPositionY, m_robotDirection);

        // Рисуем цели
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);

        // Рисуем события
        for (EventMarker marker : eventMarkers) {
            marker.draw(g2d);
        }
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        g.setColor(Color.MAGENTA);
        g.fillOval(x - 15, y - 5, 30, 10);
        g.setColor(Color.BLACK);
        g.drawOval(x - 15, y - 5, 30, 10);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.GREEN);
        g.fillOval(x - 5, y - 5, 10, 10);
    }

    // Класс для маркеров событий
    private static class EventMarker {
        private final int x, y;
        private final String description;

        public EventMarker(int x, int y, String description) {
            this.x = x;
            this.y = y;
            this.description = description;
        }

        public boolean contains(Point p) {
            return new Rectangle(x - 10, y - 10, 20, 20).contains(p);
        }

        public void draw(Graphics2D g) {
            g.setColor(Color.RED);
            g.fillRect(x - 10, y - 10, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(x - 10, y - 10, 20, 20);
        }

        public String getDescription() {
            return description;
        }
    }

    private static Timer initTimer() {
        return new Timer("events generator", true); // true делает поток демоном
    }
}