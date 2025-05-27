package com.wormfarm.farm.plant;

import com.wormfarm.farm.insect.DungBeetle;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantInstanceTest {

    @Test
    void testConstructorAndGetters() {
        PlantInstance plant = new PlantInstance(10, 20, 1000L);
        assertEquals(10, plant.getX());
        assertEquals(20, plant.getY());
        assertEquals(1000L, plant.getPlantedAtMillis());
        assertFalse(plant.isPaused());
        assertNull(plant.getAssignedBeetle());
    }

    @Test
    void testSetAssignedBeetleOnce() {
        PlantInstance plant = new PlantInstance(1, 2, 100L);
        DungBeetle beetle1 = mock(DungBeetle.class);
        DungBeetle beetle2 = mock(DungBeetle.class);
        plant.setAssignedBeetle(beetle1);
        assertEquals(beetle1, plant.getAssignedBeetle());
        plant.setAssignedBeetle(beetle2);
        assertEquals(beetle1, plant.getAssignedBeetle());
    }

    @Test
    void testSetPausedAndIsPaused() {
        PlantInstance plant = new PlantInstance(0, 0, 1000L);
        plant.setPaused(true, 2000L);
        assertTrue(plant.isPaused());
        plant.setPaused(false, 3000L);
        assertFalse(plant.isPaused());
    }

    @Test
    void testSetGlobalPausedAndResume() {
        PlantInstance plant = new PlantInstance(0, 0, 500L);
        plant.setGlobalPaused(true, 2000L);
        assertTrue(plant.isPaused());
        plant.setGlobalPaused(false, 3000L);
        assertFalse(plant.isPaused());
    }

    @Test
    void testIsReadyToHarvestAndGrowthMillis() {
        PlantInstance plant = new PlantInstance(0, 0, 1000L);
        assertFalse(plant.isReadyToHarvest(9000L));
        assertTrue(plant.isReadyToHarvest(11000L));
        assertTrue(plant.getGrowthMillis(11000L) >= 10000L);
    }

    @Test
    void testResetGrowth() {
        PlantInstance plant = new PlantInstance(0, 0, 100L);
        plant.setGrowthAccumulatedMillis(5000L);
        plant.resetGrowth(2000L);
        assertEquals(0, plant.getGrowthMillis(2000L));
        assertEquals(2000L, plant.getPlantedAtMillis());
    }

    @Test
    void testBoostGrowth() {
        PlantInstance plant = new PlantInstance(0, 0, 100L);
        plant.setGrowthAccumulatedMillis(1000L);
        plant.boostGrowth(2.0);
        assertTrue(plant.getGrowthMillis(200L) > 1000L);
    }

    @Test
    void testSetGrowthAccumulatedMillis() {
        PlantInstance plant = new PlantInstance(0, 0, 100L);
        plant.setGrowthAccumulatedMillis(7000L);
        assertEquals(7000L, plant.getGrowthMillis(100L));
    }
}