package com.wormfarm.gui.state;

import com.wormfarm.core.logic.WormSaveManager;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.core.model.AppState;
import com.wormfarm.core.model.FarmSaveData;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.dialog.LoadGameDialog;
import com.wormfarm.gui.panel.MainMenuPanel;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.settings.UserSettings;
import com.wormfarm.farm.FarmController;
import com.wormfarm.farm.resource.CompostSource;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class GameSessionManager {
    private final JFrame frame;
    private final UserSettings settings;
    private final ResourceBundle messages;
    private final JDesktopPane desktopPane;

    private WormState currentWormState;
    private WormStats currentWormStats;
    private EventMapModel currentEventMap;
    private WormMapPanel currentMapPanel;
    private String lastLoadedSaveName = null;
    private DialogManager dialogManager;

    private FarmController farmController;

    // suppression logic
    private int restoredResumableWindows = 0;
    private long suppressResumableWindowsUntil = 0;

    private Runnable onLocaleChange = null;
    public void setOnLocaleChange(Runnable onLocaleChange) {
        this.onLocaleChange = onLocaleChange;
    }
    public Runnable getOnLocaleChange() {
        return onLocaleChange;
    }

    public GameSessionManager(JFrame frame, UserSettings settings, ResourceBundle messages, JDesktopPane desktopPane) {
        this.frame = frame;
        this.settings = settings;
        this.messages = messages;
        this.desktopPane = desktopPane;
    }

    public void setRestoredResumableWindows(int n) {
        this.restoredResumableWindows = n;
    }

    public void onResumableWindowClosed() {
        if (getCurrentMapPanel() != null) getCurrentMapPanel().resumeGame();
        if (restoredResumableWindows > 0) {
            restoredResumableWindows--;
            if (restoredResumableWindows == 0) {
                suppressResumableWindowsUntil = System.currentTimeMillis() + 5000;
            }
        }
    }

    public boolean canActivateEvent() {
        if (restoredResumableWindows > 0) return false;
        return System.currentTimeMillis() >= suppressResumableWindowsUntil;
    }

    public void shutdownCurrentMapPanel() {
        if (currentMapPanel != null) {
            currentMapPanel.shutdown();
            currentMapPanel = null;
        }
    }

    private void clearDesktopPaneAndShutdown() {
        shutdownCurrentMapPanel();
        desktopPane.removeAll();
        desktopPane.revalidate();
        desktopPane.repaint();
    }

    public void showMainMenu() {
        if (farmController != null) {
            farmController.setGlobalPaused(true);
        }
        clearDesktopPaneAndShutdown();
        frame.setContentPane(new MainMenuPanel(
                this::continueGame,
                this::startNewGame,
                this::loadGame,
                () -> { if (onLocaleChange != null) onLocaleChange.run(); },
                this::exitGame,
                !WormSaveManager.listSaves().isEmpty(),
                settings
        ));
        frame.revalidate();
        frame.repaint();
    }

    public void startNewGame() {
        clearDesktopPaneAndShutdown();
        currentWormState = new WormState(100, 100, 0);
        currentEventMap = new EventMapModel();
        currentWormStats = new WormStats(0);

        WormStatsManager statsManager = new WormStatsManager(currentWormStats);

        CompostSource compostSource = new CompostSource(600, 600, 30);

        // --- Корректно останавливаем старый FarmController ---
        if (farmController != null) {
            farmController.shutdown();
        }
        farmController = new FarmController(compostSource, statsManager);

        farmController.setGlobalPaused(false);

        currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, frame, statsManager, settings, farmController);
        currentMapPanel.setGameSessionManager(this);
        currentMapPanel.setOnLanguageChanged(getOnLocaleChange());
        currentMapPanel.setDesktopPane(desktopPane);
        currentMapPanel.setOnExitToMenu(this::showMainMenu);
        currentMapPanel.setOnExitToDesktop(this::exitGame);

        frame.setContentPane(desktopPane);
        currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
        desktopPane.add(currentMapPanel, JLayeredPane.DEFAULT_LAYER);

        frame.revalidate();
        frame.repaint();
        currentMapPanel.requestFocusInWindow();

        lastLoadedSaveName = null;
    }

    public void continueGame() {
        if (currentWormState != null && currentWormStats != null) {
            clearDesktopPaneAndShutdown();
            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, frame, statsManager, settings, farmController);
            currentMapPanel.setGameSessionManager(this);
            currentMapPanel.setOnLanguageChanged(getOnLocaleChange());
            currentMapPanel.setDesktopPane(desktopPane);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);
            currentMapPanel.setOnExitToDesktop(this::exitGame);

            frame.setContentPane(desktopPane);
            currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
            desktopPane.add(currentMapPanel, JLayeredPane.DEFAULT_LAYER);

            frame.revalidate();
            frame.repaint();
            currentMapPanel.requestFocusInWindow();
        } else {
            List<String> saves = WormSaveManager.listSaves();
            if (saves.isEmpty()) {
                JOptionPane.showMessageDialog(frame, messages.getString("pause.no.saves"));
                return;
            }
            loadSpecificGame(saves.get(saves.size() - 1));
        }
    }

    public void loadGame() {
        List<String> saves = WormSaveManager.listSaves();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(frame, messages.getString("no.saves.available"));
            return;
        }

        pauseGameIfPossible();

        LoadGameDialog dialog = new LoadGameDialog(frame, saves);
        dialog.setVisible(true);

        String selectedName = dialog.getSelectedName();
        if (selectedName != null) {
            loadSpecificGame(selectedName);
        } else {
            resumeGameIfPossible();
        }
    }

    public void loadSpecificGame(String saveName) {
        try {
            clearDesktopPaneAndShutdown();
            currentWormState = new WormState(100, 100, 0);
            currentEventMap = new EventMapModel();
            currentWormStats = new WormStats(0);

            WormStatsManager statsManager = new WormStatsManager(currentWormStats);

            CompostSource compostSource = new CompostSource(600, 600, 30);

            // --- Корректно останавливаем старый FarmController ---
            if (farmController != null) {
                farmController.shutdown();
            }
            farmController = new FarmController(compostSource, statsManager);

            WormSaveManager.load(
                    currentWormState,
                    currentWormStats,
                    (x, y) -> {
                        if (currentMapPanel != null) {
                            currentMapPanel.setTarget(x, y);
                        }
                    },
                    (farmSave, found) -> {
                        if (farmSave != null) farmController.restoreFromSave(farmSave);
                        else farmController.restoreFromSave(new FarmSaveData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
                        farmController.setGlobalPaused(false);
                    },
                    saveName
            );

            currentMapPanel = new WormMapPanel(currentWormState, currentEventMap, frame, statsManager, settings, farmController);
            currentMapPanel.setGameSessionManager(this);
            currentMapPanel.setOnLanguageChanged(getOnLocaleChange());
            currentMapPanel.setDesktopPane(desktopPane);
            currentMapPanel.setOnExitToMenu(this::showMainMenu);
            currentMapPanel.setOnExitToDesktop(this::exitGame);

            frame.setContentPane(desktopPane);
            currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
            desktopPane.add(currentMapPanel, JLayeredPane.DEFAULT_LAYER);

            frame.revalidate();
            frame.repaint();
            currentMapPanel.requestFocusInWindow();

            lastLoadedSaveName = saveName;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    messages.getString("error.load.save") + ": " + e.getMessage());
        }
    }

    public void saveCurrentGame(String saveName) {
        try {
            WormSaveManager.save(
                    currentWormState,
                    currentWormStats,
                    getTargetX(),
                    getTargetY(),
                    farmController != null ? farmController.toSaveData() : new FarmSaveData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                    saveName
            );
            lastLoadedSaveName = saveName;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Ошибка сохранения: " + ex.getMessage());
        }
    }

    public void setGameStateWithFarm(WormState state, WormStatsManager statsManager, FarmSaveData farmSave) {
        clearDesktopPaneAndShutdown();
        this.currentWormState = state;
        this.currentWormStats = statsManager.getStats();
        this.currentEventMap = new EventMapModel();

        CompostSource compostSource = new CompostSource(600, 600, 30);

        // --- Корректно останавливаем старый FarmController ---
        if (this.farmController != null) {
            this.farmController.shutdown();
        }
        this.farmController = new FarmController(compostSource, statsManager);
        if (farmSave != null) {
            this.farmController.restoreFromSave(farmSave);
        } else {
            this.farmController.restoreFromSave(new FarmSaveData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }
        this.farmController.setGlobalPaused(false);

        WormStatsManager sm = new WormStatsManager(this.currentWormStats);
        this.currentMapPanel = new WormMapPanel(this.currentWormState, this.currentEventMap, frame, sm, settings, farmController);
        this.currentMapPanel.setGameSessionManager(this);
        this.currentMapPanel.setOnLanguageChanged(getOnLocaleChange());
        this.currentMapPanel.setDesktopPane(desktopPane);
        this.currentMapPanel.setOnExitToMenu(this::showMainMenu);
        this.currentMapPanel.setOnExitToDesktop(this::exitGame);
        frame.setContentPane(desktopPane);
        currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
        desktopPane.add(currentMapPanel, JLayeredPane.DEFAULT_LAYER);
        frame.revalidate();
        frame.repaint();
        currentMapPanel.requestFocusInWindow();
    }

    public void openSettings() {
        pauseGameIfPossible();
        resumeGameIfPossible();
    }

    public void setGameState(WormState state, WormStatsManager statsManager) {
        setGameStateWithFarm(state, statsManager, null);
    }

    public void exitGame() {
        shutdownCurrentMapPanel();
        saveAppState();
        System.exit(0);
    }

    public void saveAppState() {
        AppState.Mode mode;
        String lastSave = null;
        if (isGameMapActive()) {
            mode = AppState.Mode.GAME_MAP;
            lastSave = lastLoadedSaveName;
        } else {
            mode = AppState.Mode.MAIN_MENU;
        }
        AppState state = new AppState(mode, lastSave);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app.state.bin"))) {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restoreAppState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("app.state.bin"))) {
            AppState state = (AppState) ois.readObject();
            if (state.mode == AppState.Mode.MAIN_MENU) {
                showMainMenu();
            } else if (state.mode == AppState.Mode.GAME_MAP) {
                if (state.lastSaveName != null) {
                    loadSpecificGame(state.lastSaveName);
                } else {
                    startNewGame();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMainMenu();
        }
    }

    public void resizeCurrentMapPanel() {
        if (currentMapPanel != null) {
            currentMapPanel.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
        }
    }

    public boolean isGameMapActive() {
        return frame.getContentPane() == desktopPane && currentMapPanel != null;
    }

    public void pauseGameIfPossible() {
        if (currentMapPanel != null) currentMapPanel.pauseGame();
    }

    public void resumeGameIfPossible() {
        if (currentMapPanel != null) currentMapPanel.resumeGame();
    }

    public UserSettings getSettings() {
        return settings;
    }

    public List<String> getAvailableSaves() {
        return WormSaveManager.listSaves();
    }

    public WormState getWormState() {
        return currentWormState;
    }

    public WormStatsManager getStatsManager() {
        if (currentMapPanel != null) {
            return currentMapPanel.getStatsManager();
        }
        return new WormStatsManager(currentWormStats);
    }

    public int getTargetX() {
        if (currentMapPanel != null) {
            return currentMapPanel.getTargetX();
        }
        return 0;
    }

    public int getTargetY() {
        if (currentMapPanel != null) {
            return currentMapPanel.getTargetY();
        }
        return 0;
    }

    public WormMapPanel getCurrentMapPanel() {
        return currentMapPanel;
    }

    public void setDialogManager(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public FarmController getFarmController() {
        return farmController;
    }
}