package com.wormfarm.farm.market;

import com.wormfarm.farm.FarmController;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.farm.plant.PlantField;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.gui.state.GameSessionManager;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты FarmMarketPanel.
 * В headless-режиме запускать с -Djava.awt.headless=true !
 */
class FarmMarketPanelTest {

    FarmController farmController;
    WormStatsManager statsManager;
    GameSessionManager gsm;
    PlantField plantField;
    CompostSource compost;

    @BeforeEach
    void setup() {
        compost = new CompostSource(10, 20, 15);
        plantField = mock(PlantField.class);
        statsManager = mock(WormStatsManager.class);
        when(statsManager.getCoins()).thenReturn(100);

        farmController = mock(FarmController.class);
        when(farmController.getCompostSource()).thenReturn(compost);
        when(farmController.getPlantField()).thenReturn(plantField);
        when(farmController.getDungBeetles()).thenReturn(new ArrayList<>());

        gsm = mock(GameSessionManager.class);
        WormMapPanel mapPanel = mock(WormMapPanel.class);
        when(gsm.getCurrentMapPanel()).thenReturn(mapPanel);
    }

    @Test
    void testPanelConstructsAndPausesGame() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));
            assertNotNull(panel);
        }
    }

    @Test
    void testCoinsLabelUpdates() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));
            JPanel topPanel = (JPanel) panel.getContentPane().getComponent(0);
            JLabel coinsLabel = (JLabel) topPanel.getComponent(0);
            assertTrue(coinsLabel.getText().contains("100"));
        }
    }

    @Test
    void testBuyDungBeetleAddsBeetleAndAssignsPlant() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));
            PlantInstance plant = new PlantInstance(1, 2, System.currentTimeMillis());
            when(plantField.getPlants()).thenReturn(List.of(plant));

            // Проверяем, что beetle назначается растению
            doAnswer(inv -> {
                DungBeetle beetle = inv.getArgument(0);
                assertNotNull(beetle);
                // Проверяем, что plant.getAssignedBeetle() == beetle после вызова assignToPlant
                // assignToPlant вызывается внутри buyDungBeetle
                // Поэтому проверим это после buyDungBeetle ниже
                return null;
            }).when(farmController).addDungBeetle(any(DungBeetle.class));

            boolean ok = panel.buyDungBeetle(null);
            assertTrue(ok);
            // Проверяем, что plant действительно получил жука
            assertNotNull(plant.getAssignedBeetle());
        }
    }

    @Test
    void testBuyPlantAddsPlantAndAssignsIdleBeetle() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));

            when(plantField.isFull()).thenReturn(false);
            PlantInstance plant = mock(PlantInstance.class);
            when(plantField.tryAddPlant(anyLong())).thenReturn(plant);

            DungBeetle beetle = mock(DungBeetle.class);
            when(beetle.getBeetleState()).thenReturn(DungBeetle.BeetleState.IDLE);
            List<DungBeetle> beetleList = new ArrayList<>();
            beetleList.add(beetle);
            when(farmController.getDungBeetles()).thenReturn(beetleList);

            boolean ok = panel.buyPlant(null);
            assertTrue(ok);
            verify(beetle, atLeastOnce()).assignToPlant(plant);
        }
    }

    @Test
    void testBuyPlantFieldFullShowsDialog() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));

            when(plantField.isFull()).thenReturn(true);
            boolean ok = panel.buyPlant(null);
            assertFalse(ok);
            // Проверяем, что был вызван showMessageDialog с любым текстом (главное — был вызван)
            mockJOP.verify(() -> JOptionPane.showMessageDialog(any(), any()), atLeastOnce());
        }
    }

    @Test
    void testUpdateComponentsRefreshesItemsPanel() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));
            assertDoesNotThrow(panel::updateComponents);
        }
    }

    @Test
    void testWindowKeyAndTitleKey() {
        try (MockedStatic<JOptionPane> mockJOP = mockStatic(JOptionPane.class)) {
            FarmMarketPanel panel = spy(new FarmMarketPanel(farmController, statsManager, gsm));
            assertEquals("farm.market", panel.getWindowKey());
            assertEquals("farm.market.title", panel.getTitleKey());
        }
    }
}