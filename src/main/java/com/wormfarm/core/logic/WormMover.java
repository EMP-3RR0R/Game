package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormState;

public class WormMover {
    private static final double MAX_VELOCITY = 0.1;
    private static final double BASE_ANGULAR_VELOCITY = 0.001;
    private static final double MAX_ANGULAR_VELOCITY = 0.03;
    private static final double WALL_PUSH_BACK = 2.0;

    public static double distance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    public static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;
        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    public static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static double asNormalizedRadians(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    public static void moveWorm(WormState worm, double targetX, double targetY, double duration, int fieldWidth, int fieldHeight) {
        double dist = distance(targetX, targetY, worm.getX(), worm.getY());
        if (dist < 0.5) return;

        double velocity = MAX_VELOCITY;
        double angleToTarget = angleTo(worm.getX(), worm.getY(), targetX, targetY);

        double deltaAngle = angleToTarget - worm.getDirection();
        deltaAngle = Math.atan2(Math.sin(deltaAngle), Math.cos(deltaAngle));

        double proximity = Math.max(0.001, dist);
        double angleAbs = Math.abs(deltaAngle);

        double proximityNorm = 1.0 - Math.min(proximity / 200.0, 1.0);
        double angleNorm = Math.sin(angleAbs / 2);
        double dynamic = proximityNorm * angleNorm;

        double angularVelocity =
                Math.signum(deltaAngle) *
                        applyLimits(BASE_ANGULAR_VELOCITY + dynamic * (MAX_ANGULAR_VELOCITY - BASE_ANGULAR_VELOCITY),
                                BASE_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);

        velocity = applyLimits(velocity, 0, MAX_VELOCITY);

        double newX = worm.getX() + velocity / angularVelocity *
                (Math.sin(worm.getDirection() + angularVelocity * duration) - Math.sin(worm.getDirection()));
        if (!Double.isFinite(newX)) {
            newX = worm.getX() + velocity * duration * Math.cos(worm.getDirection());
        }
        double newY = worm.getY() - velocity / angularVelocity *
                (Math.cos(worm.getDirection() + angularVelocity * duration) - Math.cos(worm.getDirection()));
        if (!Double.isFinite(newY)) {
            newY = worm.getY() + velocity * duration * Math.sin(worm.getDirection());
        }

        boolean hitWall = false;

        if (newX < 0) {
            newX = WALL_PUSH_BACK;
            hitWall = true;
        } else if (newX > fieldWidth) {
            newX = fieldWidth - WALL_PUSH_BACK;
            hitWall = true;
        }

        if (newY < 0) {
            newY = WALL_PUSH_BACK;
            hitWall = true;
        } else if (newY > fieldHeight) {
            newY = fieldHeight - WALL_PUSH_BACK;
            hitWall = true;
        }

        if (hitWall) {
            double randomTurn = Math.toRadians(60 + Math.random() * 60);
            if (Math.random() < 0.5) randomTurn = -randomTurn;
            double newDirection = asNormalizedRadians(worm.getDirection() + randomTurn);
            newX = applyLimits(newX + WALL_PUSH_BACK * Math.cos(newDirection), 0, fieldWidth);
            newY = applyLimits(newY + WALL_PUSH_BACK * Math.sin(newDirection), 0, fieldHeight);
            worm.setDirection(newDirection);
        } else {
            double newDirection = asNormalizedRadians(worm.getDirection() + angularVelocity * duration);
            worm.setDirection(newDirection);
        }

        worm.setX(newX);
        worm.setY(newY);
    }
}