package com.wormfarm.farm.insect;

import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.farm.resource.CompostSource;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DungBeetleTest {

    CompostSource compost;
    DungBeetle beetle;

    @BeforeEach
    void setup() {
        compost = new CompostSource(50, 50, 10);
        beetle = new DungBeetle(10, 10, compost, 3, 2, 1.5);
    }

    @Test
    void testAssignToPlantSetsPlantAndState() {
        PlantInstance plant = new PlantInstance(20, 25, System.currentTimeMillis());
        beetle.assignToPlant(plant);
        assertEquals(plant, beetle.getTargetPlant());
        assertEquals(beetle, plant.getAssignedBeetle());
        assertEquals(DungBeetle.BeetleState.TO_COMPOST, beetle.getBeetleState());
        assertEquals(FarmInsect.State.MOVING, beetle.getState());
        assertEquals(compost.getX(), beetle.getTargetX());
        assertEquals(compost.getY(), beetle.getTargetY());
    }

    @Test
    void testTickToCompostToWithBallToPlant() {
        PlantInstance plant = new PlantInstance(52, 52, System.currentTimeMillis());
        beetle.assignToPlant(plant);
        // move towards compost
        beetle.tick();
        assertFalse(beetle.isCarryingBall());
        // simulate arrival at compost
        beetle.x = compost.getX();
        beetle.y = compost.getY();
        beetle.tick();
        assertTrue(beetle.isCarryingBall());
        assertEquals(DungBeetle.BeetleState.WITH_BALL_TO_PLANT, beetle.getBeetleState());
        assertEquals(plant.getX(), beetle.getTargetX());
        assertEquals(plant.getY(), beetle.getTargetY());
    }

    @Test
    void testTickWithBallToPlantArrivesAndReturns() {
        PlantInstance plant = new PlantInstance(20, 20, System.currentTimeMillis());
        beetle.assignToPlant(plant);

        // simulate already with ball, going to plant
        beetle.setBeetleState(DungBeetle.BeetleState.WITH_BALL_TO_PLANT);
        beetle.setHasBall(true);
        beetle.targetPlant = plant;
        beetle.setTarget(plant.getX(), plant.getY()); // ensure correct target
        beetle.x = plant.getX();
        beetle.y = plant.getY();
        beetle.tick();
        assertFalse(beetle.isCarryingBall());
        assertEquals(DungBeetle.BeetleState.TO_COMPOST_NO_BALL, beetle.getBeetleState());
        assertEquals(compost.getX(), beetle.getTargetX());
        assertEquals(compost.getY(), beetle.getTargetY());
    }

    @Test
    void testTickWithBallToPlantNoPlant() {
        beetle.setBeetleState(DungBeetle.BeetleState.WITH_BALL_TO_PLANT);
        beetle.setHasBall(true);
        beetle.targetPlant = null;
        beetle.tick();
        assertEquals(DungBeetle.BeetleState.TO_COMPOST_NO_BALL, beetle.getBeetleState());
        assertFalse(beetle.isCarryingBall());
        assertEquals(compost.getX(), beetle.getTargetX());
    }

    @Test
    void testTickToCompostNoBallCycles() {
        beetle.setBeetleState(DungBeetle.BeetleState.TO_COMPOST_NO_BALL);
        beetle.setTarget(compost.getX(), compost.getY());
        beetle.x = compost.getX();
        beetle.y = compost.getY();
        beetle.tick();
        assertEquals(DungBeetle.BeetleState.TO_COMPOST, beetle.getBeetleState());
        assertEquals(FarmInsect.State.MOVING, beetle.getState());
    }

    @Test
    void testMoveToTarget() {
        beetle.setTarget(20, 10);
        beetle.x = 10;
        beetle.y = 10;
        beetle.moveToTarget(3);
        assertEquals(13, beetle.getX());
        assertEquals(10, beetle.getY());
    }

    @Test
    void testShouldDrawBall() {
        beetle.setBeetleState(DungBeetle.BeetleState.WITH_BALL_TO_PLANT);
        beetle.setHasBall(true);
        assertTrue(beetle.shouldDrawBall());
        beetle.setHasBall(false);
        assertFalse(beetle.shouldDrawBall());
    }

    @Test
    void testGetBallPosition() {
        beetle.setBeetleState(DungBeetle.BeetleState.WITH_BALL_TO_PLANT);
        beetle.setHasBall(true);
        beetle.setTarget(26, 10);
        beetle.x = 10;
        beetle.y = 10;
        int[] pos = beetle.getBallPosition();
        assertNotNull(pos);
        assertTrue(pos[0] != beetle.x || pos[1] != beetle.y, "Ball position must differ from beetle position at least along one axis");
    }

    @Test
    void testGetBallPositionNullIfNotDrawing() {
        beetle.setBeetleState(DungBeetle.BeetleState.TO_COMPOST);
        beetle.setHasBall(true);
        assertNull(beetle.getBallPosition());
    }
}