package com.wormfarm.gui.state;

import com.wormfarm.gui.base.WindowStateManager;
import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WindowProfileManager {
    private final JFrame frame;
    private final ResourceBundle messages;
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
        public java.util.List<WindowStateManager.InternalFrameState> frames;
    }

    public WindowProfileManager(JFrame frame, ResourceBundle messages, JDesktopPane desktopPane, GameSessionManager gameSessionManager) {
        this.frame = frame;
        this.messages = messages;
        this.desktopPane = desktopPane;
        this.gameSessionManager = gameSessionManager;
    }

    public void saveWindowsProfile() {
        try {
            gameSessionManager.saveAppState();

            Path dir = Paths.get(PROFILES_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            // Сдвиг старых профилей
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

            windowStateManager.captureStates(desktopPane);

            MainWindowState mainWinState = new MainWindowState();
            java.awt.Rectangle b = frame.getBounds();
            mainWinState.x = b.x;
            mainWinState.y = b.y;
            mainWinState.width = b.width;
            mainWinState.height = b.height;
            mainWinState.state = frame.getExtendedState();

            FullProfile profile = new FullProfile();
            profile.mainWindow = mainWinState;
            // Убираем дубли по ключу:
            Map<String, WindowStateManager.InternalFrameState> uniqueFrames = new LinkedHashMap<>();
            for (WindowStateManager.InternalFrameState state : windowStateManager.getFrameStates()) {
                uniqueFrames.put(state.windowKey, state);
            }
            profile.frames = new ArrayList<>(uniqueFrames.values());

            File profile1 = dir.resolve("profile1.state.bin").toFile();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(profile1))) {
                out.writeObject(profile);
            }

            if (gameSessionManager.isGameMapActive()) {
                Path autoSaveDir = dir.resolve("profile1");
                Files.createDirectories(autoSaveDir);
                File autoSaveFile = autoSaveDir.resolve("autosave.bin").toFile();
                WormState worm = gameSessionManager.getWormState();
                WormStatsManager statsManager = gameSessionManager.getStatsManager();
                int targetX = gameSessionManager.getTargetX();
                int targetY = gameSessionManager.getTargetY();
                WormSaveManager.saveToAbsolutePath(
                        worm,
                        statsManager.getStats(),
                        targetX,
                        targetY,
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
            Path dir = Paths.get(PROFILES_DIR);
            File file = dir.resolve("profile" + profileNumber + ".state.bin").toFile();
            Path autoSavePath = dir.resolve("profile" + profileNumber).resolve("autosave.bin");
            boolean loadedAutoSave = false;

            if (!file.exists()) {
                JOptionPane.showMessageDialog(frame, messages.getString("profile.not.found") + " #" + profileNumber);
                return;
            }

            // Удаляем все старые окна
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

            // Загружаем профиль
            FullProfile restoredProfile;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = in.readObject();
                if (!(obj instanceof FullProfile)) throw new IOException("Некорректный профиль окна");
                restoredProfile = (FullProfile)obj;
            }

            // Восстанавливаем главное окно
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

            // Восстанавливаем автосейв если есть
            final int[] loadedTarget = new int[2];
            if (Files.exists(autoSavePath)) {
                WormState worm = new WormState(100, 100, 0);
                WormStats stats = new WormStats(0);
                WormStatsManager statsManager = new WormStatsManager(stats);
                WormSaveManager.loadFromAbsolutePath(
                        worm,
                        stats,
                        (x, y) -> { loadedTarget[0] = x; loadedTarget[1] = y; },
                        autoSavePath.toAbsolutePath().toString()
                );
                gameSessionManager.setGameState(worm, statsManager);
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

            // Восстанавливаем внутренние окна (только по одному каждого типа!)
            windowStateManager.getFrameStates().clear();
            if (restoredProfile.frames != null) {
                Map<String, WindowStateManager.InternalFrameState> uniqueFrames = new LinkedHashMap<>();
                for (WindowStateManager.InternalFrameState state : restoredProfile.frames) {
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
                    default:
                        return null;
                }
            });

            if (gameSessionManager.getCurrentMapPanel() != null) {
                gameSessionManager.getCurrentMapPanel().setTarget(targetX, targetY);
            }

            boolean hasPause = restoredWindowKeys.contains("pause.menu");
            boolean hasFifteen = restoredWindowKeys.contains("minigame.fifteen.puzzle");
            System.out.println("Восстановленные окна: " + restoredWindowKeys);

            if (hasPause || hasFifteen) {
                gameSessionManager.pauseGameIfPossible();
            } else {
                if (gameSessionManager.getCurrentMapPanel() != null) {
                    gameSessionManager.getCurrentMapPanel().resumeGame();
                }
            }

            System.out.println("Профиль окон #" + profileNumber + " восстановлен (главное окно + внутренние окна)." +
                    (loadedAutoSave ? " (автосейв подгружен)" : ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JMenuBar createMenuBarProfiles() {
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