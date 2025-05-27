package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WormMoverTest {

    @Test
    void testDistance() {
        assertEquals(5.0, WormMover.distance(0, 0, 3, 4), 1e-8);
        assertEquals(0.0, WormMover.distance(2, 2, 2, 2), 1e-8);
    }

    @Test
    void testAngleTo() {
        assertEquals(0.0, WormMover.angleTo(0, 0, 1, 0), 1e-8);
        assertEquals(Math.PI / 2, WormMover.angleTo(0, 0, 0, 1), 1e-8);
        assertEquals(Math.PI, WormMover.angleTo(0, 0, -1, 0), 1e-8);
        assertEquals(3 * Math.PI / 2, WormMover.angleTo(0, 0, 0, -1), 1e-8);
    }

    @Test
    void testApplyLimits() {
        assertEquals(2.0, WormMover.applyLimits(1.0, 2.0, 5.0));
        assertEquals(4.0, WormMover.applyLimits(4.0, 2.0, 5.0));
        assertEquals(5.0, WormMover.applyLimits(6.0, 2.0, 5.0));
    }

    @Test
    void testAsNormalizedRadians() {
        assertEquals(0.0, WormMover.asNormalizedRadians(0.0), 1e-8);
        assertEquals(Math.PI, WormMover.asNormalizedRadians(Math.PI), 1e-8);
        assertEquals(0.0, WormMover.asNormalizedRadians(2 * Math.PI), 1e-8);
        assertEquals(Math.PI / 2, WormMover.asNormalizedRadians(-3 * Math.PI / 2), 1e-8);
    }

    @Test
    void testMoveWorm_BasicMovement() {
        WormState worm = new WormState(10, 10, 0);
        WormMover.moveWorm(worm, 100, 10, 1, 200, 200);

        assertTrue(worm.getX() > 10);
        assertEquals(10, worm.getY(), 1.0);
        assertTrue(worm.getDirection() >= 0 && worm.getDirection() <= 2 * Math.PI);
    }

    @Test
    void testMoveWorm_TooClose_NoMove() {
        WormState worm = new WormState(5, 5, 0);
        WormMover.moveWorm(worm, 5.2, 5.2, 1, 200, 200);
        assertEquals(5, worm.getX(), 1e-8);
        assertEquals(5, worm.getY(), 1e-8);
    }

    @Test
    void testMoveWorm_WallCollision_Left() {
        WormState worm = new WormState(1, 100, Math.PI);
        WormMover.moveWorm(worm, -10, 100, 1, 200, 200);
        assertTrue(worm.getX() >= 0);
        assertTrue(worm.getX() <= 200);
        assertTrue(worm.getY() >= 0 && worm.getY() <= 200);
    }

    @Test
    void testMoveWorm_WallCollision_Top() {
        WormState worm = new WormState(100, 1, -Math.PI / 2);
        WormMover.moveWorm(worm, 100, -10, 1, 200, 200);
        assertTrue(worm.getY() >= 0);
        assertTrue(worm.getX() >= 0 && worm.getX() <= 200);
    }

    @Test
    void testMoveWorm_WallCollision_Right() {
        WormState worm = new WormState(199, 10, 0);
        WormMover.moveWorm(worm, 250, 10, 1, 200, 200);
        assertTrue(worm.getX() <= 200);
        assertTrue(worm.getX() >= 0);
    }

    @Test
    void testMoveWorm_WallCollision_Bottom() {
        WormState worm = new WormState(10, 199, Math.PI / 2);
        WormMover.moveWorm(worm, 10, 250, 1, 200, 200);
        assertTrue(worm.getY() <= 200);
        assertTrue(worm.getY() >= 0);
    }
}