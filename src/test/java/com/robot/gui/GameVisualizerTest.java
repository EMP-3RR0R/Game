package com.robot.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class GameVisualizerTest {
    private GameVisualizer gameVisualizer;

    @BeforeEach
    public void setUp() {
        gameVisualizer = new GameVisualizer();
        gameVisualizer.setSize(400, 400);
        gameVisualizer.setVisible(true);
    }

    @Test
    public void testInitialSetup() {
        assertNotNull(gameVisualizer);
        assertEquals(101, gameVisualizer.getRobotPositionX(), 0.1);
        assertEquals(100, gameVisualizer.getRobotPositionY(), 0.1);
        assertEquals(0, gameVisualizer.getRobotDirection(), 0.1);
        assertEquals(150, gameVisualizer.getTargetPositionX());
        assertEquals(100, gameVisualizer.getTargetPositionY());
    }

    @Test
    public void testSetTargetPosition() {
        Point newTarget = new Point(200, 200);
        gameVisualizer.setTargetPosition(newTarget);
        assertEquals(200, gameVisualizer.getTargetPositionX());
        assertEquals(200, gameVisualizer.getTargetPositionY());
    }

    @Test
    public void testMouseClicked() {
        Point clickPoint = new Point(250, 250);
        MouseEvent mouseEvent = new MouseEvent(gameVisualizer, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, clickPoint.x, clickPoint.y, 1, false);
        gameVisualizer.dispatchEvent(mouseEvent);
        assertEquals(250, gameVisualizer.getTargetPositionX());
        assertEquals(250, gameVisualizer.getTargetPositionY());
    }

    @Test
    public void testDistanceCalculation() {
        double dist = GameVisualizer.distance(0, 0, 3, 4);
        assertEquals(5.0, dist);
    }

    @Test
    public void testAngleToCalculation() {
        double angle = GameVisualizer.angleTo(0, 0, 1, 1);
        assertEquals(Math.PI / 4, angle, 0.0001);
    }

    @Test
    public void testMoveRobot() {
        gameVisualizer.setRobotPositionX(100);
        gameVisualizer.setRobotPositionY(100);
        gameVisualizer.moveRobot(0.1, 0, 10);
        assertNotEquals(100, gameVisualizer.getRobotPositionX());
    }

    @Test
    public void testApplyLimits() {
        double limitedVal = GameVisualizer.applyLimits(1.5, 0, 1);
        assertEquals(1, limitedVal);
    }

    @Test
    public void testOnModelUpdateEvent() {
        gameVisualizer.setTargetPositionX(200);
        gameVisualizer.setTargetPositionY(200);
        gameVisualizer.onModelUpdateEvent();
        assertNotEquals(100, gameVisualizer.getRobotPositionX());
        assertNotEquals(100, gameVisualizer.getRobotPositionY());
    }

    @Test
    public void testRedrawEvent() {
        gameVisualizer.onRedrawEvent();
        assertFalse(SwingUtilities.isEventDispatchThread());
    }

    @Test
    public void testPaint() {
        Graphics g = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB).getGraphics();
        gameVisualizer.paint(g);
        // No exception means the paint method works correctly
    }

    @Test
    public void testAsNormalizedRadians() {
        double normalizedAngle = GameVisualizer.asNormalizedRadians(-Math.PI);
        assertEquals(Math.PI, normalizedAngle, 0.0001);
    }
}