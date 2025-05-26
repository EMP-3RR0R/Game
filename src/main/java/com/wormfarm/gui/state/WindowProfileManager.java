package com.wormfarm.gui.state;

import com.wormfarm.gui.base.WindowStateManager;
import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.gui.base.InternalFrameState;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.FarmSaveData;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.settings.UserSettings;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.farm.FarmController;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WindowProfileManager {
    private final JFrame frame;
    private final JDesktopPane desktopPane;
    private final GameSessionManager gameSessionManager;
    private final WindowStateManager windowStateManager = new WindowStateManager();
    private static final String PROFILES_DIR = "window_profiles";
    private static final int MAX_PROFILES = 3;

    public static class MainWindowState implements Serializable {
        public int x, y, width, height, state;
    }

    public static class FullProfile implements Serializable {
        public MainWindowState mainWindow;
        public java.util.List<InternalFrameState> frames;
    }

    public WindowProfileManager(JFrame frame, JDesktopPane desktopPane, GameSessionManager gameSessionManager) {
        this.frame = frame;
        this.desktopPane = desktopPane;
        this.gameSessionManager = gameSessionManager;
    }

    public void saveWindowsProfile() {
        try {
            windowStateManager.captureStates(desktopPane);

            Path dir = Paths.get(PROFILES_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            for (int i = MAX_PROFILES - 1; i >= 1; i--) {
                Path prev = dir.resolve("profile" + i + ".state.bin");
                Path next = dir.resolve("profile" + (i + 1) + ".state.bin");
                if (Files.exists(prev)) {
                    Files.move(prev, next, StandardCopyOption.REPLACE_EXISTING);
                }
                Path prevAuto = dir.resolve("profile" + i).resolve("autosave.bin");
                Path nextAuto = dir.resolve("profile" + (i + 1)).resolve("autosave.bin");
                if (Files.exists(prevAuto)) {
                    Files.createDirectories(nextAuto.getParent());
                    Files.move(prevAuto, nextAuto, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            MainWindowState mainWinState = new MainWindowState();
            java.awt.Rectangle b = frame.getBounds();
            mainWinState.x = b.x;
            mainWinState.y = b.y;
            mainWinState.width = b.width;
            mainWinState.height = b.height;
            mainWinState.state = frame.getExtendedState();

            FullProfile profile = new FullProfile();
            profile.mainWindow = mainWinState;
            Map<String, InternalFrameState> uniqueFrames = new LinkedHashMap<>();
            for (InternalFrameState state : windowStateManager.getFrameStates()) {
                uniqueFrames.put(state.windowKey, state);
            }
            profile.frames = new ArrayList<>(uniqueFrames.values());

            File profile1 = dir.resolve("profile1.state.bin").toFile();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(profile1))) {
                out.writeObject(profile);
            }

            gameSessionManager.saveAppState();

            if (gameSessionManager.isGameMapActive()) {
                Path autoSaveDir = dir.resolve("profile1");
                Files.createDirectories(autoSaveDir);
                File autoSaveFile = autoSaveDir.resolve("autosave.bin").toFile();
                WormState worm = gameSessionManager.getWormState();
                WormStatsManager statsManager = gameSessionManager.getStatsManager();
                int targetX = gameSessionManager.getTargetX();
                int targetY = gameSessionManager.getTargetY();
                FarmSaveData farmSaveData = gameSessionManager.getFarmController() != null
                        ? gameSessionManager.getFarmController().toSaveData()
                        : new FarmSaveData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                WormSaveManager.saveToAbsolutePath(
                        worm,
                        statsManager.getStats(),
                        targetX,
                        targetY,
                        farmSaveData,
                        autoSaveFile.getAbsolutePath()
                );
                System.out.println("Автосейв профиля сохранён в " + autoSaveFile.getAbsolutePath());
            }

            System.out.println("Состояние окон и основного окна сохранено в " + profile1.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreWindowsProfile(int profileNumber) {
        try {
            ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
            Path dir = Paths.get(PROFILES_DIR);
            File file = dir.resolve("profile" + profileNumber + ".state.bin").toFile();
            Path autoSavePath = dir.resolve("profile" + profileNumber).resolve("autosave.bin");
            boolean loadedAutoSave = false;

            if (!file.exists()) {
                JOptionPane.showMessageDialog(frame, messages.getString("profile.not.found") + " #" + profileNumber);
                return;
            }

            for (JInternalFrame oldFrame : desktopPane.getAllFrames()) {
                if (oldFrame instanceof BaseInternalFrame) {
                    ((BaseInternalFrame) oldFrame).closeWithoutConfirmation();
                } else {
                    try { oldFrame.setClosed(true); } catch (Exception ignored) {}
                }
                try {
                    if (oldFrame instanceof AutoCloseable) {
                        ((AutoCloseable) oldFrame).close();
                    }
                } catch (Exception ignored) {}
                desktopPane.remove(oldFrame);
            }
            gameSessionManager.shutdownCurrentMapPanel();
            desktopPane.repaint();

            FullProfile restoredProfile;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = in.readObject();
                if (!(obj instanceof FullProfile)) throw new IOException("Некорректный профиль окна");
                restoredProfile = (FullProfile)obj;
            }

            if (restoredProfile.mainWindow != null) {
                int newX = restoredProfile.mainWindow.x;
                int newY = restoredProfile.mainWindow.y;
                int newW = restoredProfile.mainWindow.width;
                int newH = restoredProfile.mainWindow.height;
                int frameState = restoredProfile.mainWindow.state;

                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                java.awt.Rectangle screen = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
                if (newX < screen.x || newY < screen.y ||
                        newX > screen.x + screen.width - 50 ||
                        newY > screen.y + screen.height - 50)
                {
                    newX = 50;
                    newY = 50;
                }
                if (newW < 100) newW = 800;
                if (newH < 100) newH = 600;
                frame.setBounds(newX, newY, newW, newH);
                frame.setExtendedState(frameState);
            }

            final int[] loadedTarget = new int[2];
            final FarmSaveData[] loadedFarm = new FarmSaveData[1];
            if (Files.exists(autoSavePath)) {
                WormState worm = new WormState(100, 100, 0);
                WormStats stats = new WormStats(0);
                WormStatsManager statsManager = new WormStatsManager(stats);
                WormSaveManager.loadFromAbsolutePath(
                        worm,
                        stats,
                        (x, y) -> { loadedTarget[0] = x; loadedTarget[1] = y; },
                        (farmSave, found) -> { loadedFarm[0] = farmSave; },
                        autoSavePath.toAbsolutePath().toString()
                );
                gameSessionManager.setGameStateWithFarm(worm, statsManager, loadedFarm[0]);
                loadedAutoSave = true;
                System.out.println("Загружен автосейв профиля: " + autoSavePath.toAbsolutePath());
            } else {
                gameSessionManager.restoreAppState();
            }

            WormState worm = gameSessionManager.getWormState();
            WormStatsManager statsManager = gameSessionManager.getStatsManager();
            int targetX = loadedTarget[0];
            int targetY = loadedTarget[1];
            UserSettings settings = gameSessionManager.getSettings();

            windowStateManager.getFrameStates().clear();
            if (restoredProfile.frames != null) {
                Map<String, InternalFrameState> uniqueFrames = new LinkedHashMap<>();
                for (InternalFrameState state : restoredProfile.frames) {
                    uniqueFrames.put(state.windowKey, state);
                }
                windowStateManager.getFrameStates().addAll(uniqueFrames.values());
            }

            List<String> restoredWindowKeys = windowStateManager.restoreStates(desktopPane, (windowKey, state) -> {
                switch (windowKey) {
                    case "pause.menu":
                        System.out.println("Восстанавливаем окно: pause.menu");
                        return new com.wormfarm.gui.dialog.PauseMenuDialog(
                                frame,
                                null,
                                null,
                                null,
                                worm,
                                statsManager,
                                targetX,
                                targetY,
                                settings,
                                null,
                                frame,
                                gameSessionManager
                        );
                    case "minigame.fifteen.puzzle":
                        System.out.println("Восстанавливаем окно: minigame.fifteen.puzzle");
                        return new com.wormfarm.minigames.fifteenpuzzle.ui.swing.FifteenPuzzleFrame(gameSessionManager);
                    case "farm.market":
                        System.out.println("Восстанавливаем окно: farm.market");
                        FarmController farmController = gameSessionManager.getFarmController();
                        WormStatsManager farmStatsManager = statsManager;
                        if (farmController != null && farmStatsManager != null) {
                            return new com.wormfarm.farm.market.FarmMarketPanel(farmController, farmStatsManager, gameSessionManager);
                        } else {
                            System.out.println("Невозможно восстановить окно farm.market: нет контроллера или statsManager");
                            return null;
                        }
                    default:
                        return null;
                }
            });

            if (gameSessionManager.getCurrentMapPanel() != null) {
                gameSessionManager.getCurrentMapPanel().setTarget(targetX, targetY);
                gameSessionManager.getCurrentMapPanel().suppressMarketOnRestoreOnce();
            }

            boolean hasPause = restoredWindowKeys.contains("pause.menu");
            boolean hasFifteen = restoredWindowKeys.contains("minigame.fifteen.puzzle");
            boolean hasMarket = restoredWindowKeys.contains("farm.market");
            System.out.println("Восстановленные окна: " + restoredWindowKeys);

            if (hasPause || hasFifteen || hasMarket) {
                gameSessionManager.pauseGameIfPossible();
            } else {
                if (gameSessionManager.getCurrentMapPanel() != null) {
                    gameSessionManager.getCurrentMapPanel().resumeGame();
                }
            }

            Set<String> resumableKeys = new HashSet<>(Arrays.asList("pause.menu", "minigame.fifteen.puzzle", "farm.market"));
            int restoredResumableCount = 0;
            for (String key : restoredWindowKeys) {
                if (resumableKeys.contains(key)) restoredResumableCount++;
            }
            gameSessionManager.setRestoredResumableWindows(restoredResumableCount);

            System.out.println("Профиль окон #" + profileNumber + " восстановлен (главное окно + внутренние окна)." +
                    (loadedAutoSave ? " (автосейв подгружен)" : ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JMenuBar createMenuBarProfiles() {
        ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        JMenuBar menuBar = new JMenuBar();

        JMenu profilesMenu = new JMenu(messages.getString("menu.profiles"));
        JMenuItem saveProfileItem = new JMenuItem(messages.getString("menu.profiles.save"));
        saveProfileItem.addActionListener(e -> saveWindowsProfile());
        profilesMenu.add(saveProfileItem);
        profilesMenu.addSeparator();

        for (int i = 1; i <= MAX_PROFILES; i++) {
            int profileNum = i;
            JMenuItem item = new JMenuItem(messages.getString("menu.profiles.restore") + " #" + profileNum);
            item.addActionListener(e -> restoreWindowsProfile(profileNum));
            profilesMenu.add(item);
        }
        menuBar.add(profilesMenu);

        return menuBar;
    }
}