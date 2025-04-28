package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WormStatsManagerTest {

    private WormStats stats;
    private WormStatsManager manager;

    @BeforeEach
    void setUp() {
        stats = new WormStats(10);
        manager = new WormStatsManager(stats);
    }

    @Test
    void testAddCoins_Positive() {
        manager.addCoins(5);
        assertEquals(15, stats.getWormCoins());
    }

    @Test
    void testAddCoins_Zero() {
        manager.addCoins(0);
        assertEquals(10, stats.getWormCoins());
    }

    @Test
    void testAddCoins_Negative() {
        manager.addCoins(-10);
        assertEquals(10, stats.getWormCoins());
    }

    @Test
    void testSpendCoins_Success() {
        boolean result = manager.spendCoins(5);
        assertTrue(result);
        assertEquals(5, stats.getWormCoins());
    }

    @Test
    void testSpendCoins_Exact() {
        boolean result = manager.spendCoins(10);
        assertTrue(result);
        assertEquals(0, stats.getWormCoins());
    }

    @Test
    void testSpendCoins_Insufficient() {
        boolean result = manager.spendCoins(15);
        assertFalse(result);
        assertEquals(10, stats.getWormCoins());
    }

    @Test
    void testSpendCoins_NonPositive() {
        assertFalse(manager.spendCoins(0));
        assertFalse(manager.spendCoins(-2));
        assertEquals(10, stats.getWormCoins());
    }

    @Test
    void testGetCoins() {
        assertEquals(10, manager.getCoins());
        manager.addCoins(7);
        assertEquals(17, manager.getCoins());
    }

    @Test
    void testGetStats() {
        assertSame(stats, manager.getStats());
    }
}