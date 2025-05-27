package com.wormfarm.farm;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.core.model.FarmSaveData;
import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FarmControllerTest {

    WormStatsManager statsManager;
    CompostSource compostSource;
    FarmController controller;

    @BeforeEach
    void setup() {
        statsManager = mock(WormStatsManager.class);
        compostSource = new CompostSource(111, 222, 11);
        controller = new FarmController(compostSource, statsManager);
    }

    @AfterEach
    void cleanup() {
        controller.shutdown();
    }

    @Test
    void testGettersAndAddDungBeetle() {
        assertEquals(compostSource, controller.getCompostSource());
        assertNotNull(controller.getPlantField());
        assertNotNull(controller.getDungBeetles());
        assertNotNull(controller.getMarketMarker());
        assertEquals(0, controller.getDungBeetles().size());

        DungBeetle beetle = mock(DungBeetle.class);
        controller.addDungBeetle(beetle);
        assertEquals(1, controller.getDungBeetles().size());
        assertTrue(controller.getDungBeetles().contains(beetle));
    }

    @Test
    void testSetAndIsGlobalPaused() {
        assertFalse(controller.isGlobalPaused());
        controller.setGlobalPaused(true);
        assertTrue(controller.isGlobalPaused());
        controller.setGlobalPaused(false);
        assertFalse(controller.isGlobalPaused());
    }

    @Test
    void testSetStatsManager() {
        WormStatsManager newStats = mock(WormStatsManager.class);
        controller.setStatsManager(newStats);
    }

    @Test
    void testTickCallsBeetles() {
        DungBeetle beetle = mock(DungBeetle.class);
        controller.addDungBeetle(beetle);
        controller.setGlobalPaused(false);
        controller.tick();
        verify(beetle, atLeastOnce()).tick();

        controller.setGlobalPaused(true);
        controller.tick();
        verifyNoMoreInteractions(beetle);
    }

    @Test
    void testGrowthTimerAndHarvestTimer_AutoHarvest() throws Exception {
        final int[] coins = {0};
        doAnswer(inv -> {
            coins[0]++;
            return null;
        }).when(statsManager).addCoins(anyInt());

        PlantInstance plant = new PlantInstance(10, 20, System.currentTimeMillis() - 10000);
        controller.getPlantField().getPlants().add(plant);

        TimeUnit.MILLISECONDS.sleep(1200);

        assertTrue(coins[0] > 0, "autoHarvestPlants should add at least one coin");
    }

    @Test
    void testToSaveDataAndRestoreFromSave() {
        PlantInstance plant = new PlantInstance(7, 8, 123L);
        plant.setGrowthAccumulatedMillis(555L);
        controller.getPlantField().getPlants().add(plant);

        DungBeetle beetle = new DungBeetle(12, 13, compostSource, 2, 4, 3.3);
        beetle.assignToPlant(plant);
        beetle.setBeetleState(DungBeetle.BeetleState.WITH_BALL_TO_PLANT);
        beetle.setHasBall(true);
        controller.addDungBeetle(beetle);

        FarmSaveData saveData = controller.toSaveData();

        assertEquals(1, saveData.getPlants().size());
        assertEquals(1, saveData.getBeetles().size());

        FarmController ctrl2 = new FarmController(compostSource, statsManager);
        ctrl2.restoreFromSave(saveData);

        assertEquals(1, ctrl2.getPlantField().getPlants().size());
        assertEquals(1, ctrl2.getDungBeetles().size());

        PlantInstance restoredPlant = ctrl2.getPlantField().getPlants().get(0);
        DungBeetle restoredBeetle = ctrl2.getDungBeetles().get(0);
        assertEquals(7, restoredPlant.getX());
        assertEquals(8, restoredPlant.getY());
        assertEquals(DungBeetle.BeetleState.WITH_BALL_TO_PLANT, restoredBeetle.getBeetleState());
        assertTrue(restoredBeetle.isCarryingBall());
        assertEquals(restoredPlant, restoredBeetle.getTargetPlant());

        ctrl2.shutdown();
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> controller.shutdown());
    }
}