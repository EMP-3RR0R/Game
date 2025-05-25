package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.core.model.FarmSaveData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WormSaveManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setupSaveDir() {
        // Очищаем папку перед каждым тестом
        File dir = tempDir.toFile();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        WormSaveManager.setSaveDir(dir.getAbsolutePath());
    }

    @Test
    void saveFileAppearsAndHasSaveReturnsTrue() throws IOException {
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(77);
        int targetX = 123;
        int targetY = 456;
        String saveName = "testsave1";

        assertFalse(WormSaveManager.hasSave(saveName), "Save must not exist before saving");
        WormSaveManager.save(state, stats, targetX, targetY, null, saveName); // null FarmSaveData
        assertTrue(WormSaveManager.hasSave(saveName), "Save must exist after saving");
    }

    @Test
    void canSaveAndLoadStateStatsAndTarget() throws IOException, ClassNotFoundException {
        WormState originalState = new WormState(10, 20, 0.5);
        WormStats originalStats = new WormStats(15);
        int targetX = 333;
        int targetY = 444;
        String saveName = "loadsave1";
        WormSaveManager.save(originalState, originalStats, targetX, targetY, null, saveName); // null FarmSaveData

        WormState loadedState = new WormState(0, 0, 0);
        WormStats loadedStats = new WormStats(0);
        int[] loadedTarget = new int[2];

        WormSaveManager.load(loadedState, loadedStats,
                (x, y) -> {
                    loadedTarget[0] = x;
                    loadedTarget[1] = y;
                },
                (farmSave, found) -> {}, // no-op farm consumer
                saveName);

        assertAll("Loaded state/coins/targets are correct",
                () -> assertEquals(originalState.getX(), loadedState.getX(), 1e-9),
                () -> assertEquals(originalState.getY(), loadedState.getY(), 1e-9),
                () -> assertEquals(originalState.getDirection(), loadedState.getDirection(), 1e-9),
                () -> assertEquals(originalStats.getWormCoins(), loadedStats.getWormCoins()),
                () -> assertEquals(targetX, loadedTarget[0], "Target X restored"),
                () -> assertEquals(targetY, loadedTarget[1], "Target Y restored")
        );
    }

    @Test
    void listSavesReturnsAllSavedFiles() throws IOException {
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(5);
        int targetX = 100, targetY = 200;

        String save1 = "saveA";
        String save2 = "saveB";
        WormSaveManager.save(state, stats, targetX, targetY, null, save1);
        WormSaveManager.save(state, stats, targetX + 1, targetY + 1, null, save2);

        List<String> saves = WormSaveManager.listSaves();
        assertTrue(saves.contains(save1), "List must include save1");
        assertTrue(saves.contains(save2), "List must include save2");
    }

    @Test
    void deleteSaveRemovesFile() throws IOException {
        String saveName = "delsave1";
        WormState state = new WormState(10, 10, 0);
        WormStats stats = new WormStats(1);
        int targetX = 22, targetY = 33;

        WormSaveManager.save(state, stats, targetX, targetY, null, saveName);
        assertTrue(WormSaveManager.hasSave(saveName), "File should exist after save");

        WormSaveManager.deleteSave(saveName);
        assertFalse(WormSaveManager.hasSave(saveName), "File must be gone after delete");
    }

    @Test
    void saveThrowsOnInvalidSaveName() {
        WormState state = new WormState(1, 2, 3);
        WormStats stats = new WormStats(5);
        int targetX = 1, targetY = 2;
        assertThrows(IllegalArgumentException.class, () ->
                        WormSaveManager.save(state, stats, targetX, targetY, null, "invalid/name?with*chars"),
                "Invalid filename must throw exception"
        );
    }

    @Test
    void listSavesEmptyIfNoneExist() {
        // Ещё раз очищаем папку на всякий случай
        File dir = tempDir.toFile();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        WormSaveManager.setSaveDir(dir.getAbsolutePath());

        List<String> saves = WormSaveManager.listSaves();
        assertNotNull(saves, "List must not be null");
        assertTrue(saves.isEmpty(), "List must be empty if no files saved");
    }
}