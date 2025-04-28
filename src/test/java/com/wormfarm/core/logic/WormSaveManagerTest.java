package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WormSaveManagerTest {

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setupSaveDir() {
        // Используем временную директорию для тестов
        WormSaveManager.setSaveDir(tempDir.toFile().getAbsolutePath());
    }

    @Test
    void testSaveAndHasSave() throws IOException {
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(77);
        String saveName = "testsave";

        assertFalse(WormSaveManager.hasSave(saveName));
        WormSaveManager.save(state, stats, saveName);
        assertTrue(WormSaveManager.hasSave(saveName));
    }

    @Test
    void testLoad() throws IOException, ClassNotFoundException {
        WormState state = new WormState(10, 20, 0.5);
        WormStats stats = new WormStats(15);
        String saveName = "loadsave";
        WormSaveManager.save(state, stats, saveName);

        WormState loadedState = new WormState(0, 0, 0);
        WormStats loadedStats = new WormStats(0);

        WormSaveManager.load(loadedState, loadedStats, saveName);

        assertEquals(state.getX(), loadedState.getX());
        assertEquals(state.getY(), loadedState.getY());
        assertEquals(state.getDirection(), loadedState.getDirection());
        assertEquals(stats.getWormCoins(), loadedStats.getWormCoins());
    }

    @Test
    void testListSaves() throws IOException {
        String save1 = "save1";
        String save2 = "save2";
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(5);

        WormSaveManager.save(state, stats, save1);
        WormSaveManager.save(state, stats, save2);

        List<String> saves = WormSaveManager.listSaves();
        assertTrue(saves.contains(save1));
        assertTrue(saves.contains(save2));
    }

    @Test
    void testDeleteSave() throws IOException {
        String saveName = "delsave";
        WormState state = new WormState(10, 10, 0);
        WormStats stats = new WormStats(1);

        WormSaveManager.save(state, stats, saveName);
        assertTrue(WormSaveManager.hasSave(saveName));

        WormSaveManager.deleteSave(saveName);
        assertFalse(WormSaveManager.hasSave(saveName));
    }

    @Test
    void testInvalidSaveName() {
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(5);
        assertThrows(IllegalArgumentException.class, () -> WormSaveManager.save(state, stats, "invalid/save?name"));
    }
}