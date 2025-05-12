package com.wormfarm.gui.state;

import javax.swing.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class AppStateRestorer {
    private final JFrame frame;
    private final ResourceBundle messages;
    private final GameSessionManager gameSessionManager;
    private final WindowProfileManager windowProfileManager;
    private final DialogManager dialogManager;
    private final int MAX_PROFILES = 3;
    private final String PROFILES_DIR = "window_profiles";

    public AppStateRestorer(
            JFrame frame,
            ResourceBundle messages,
            GameSessionManager gameSessionManager,
            WindowProfileManager windowProfileManager,
            DialogManager dialogManager
    ) {
        this.frame = frame;
        this.messages = messages;
        this.gameSessionManager = gameSessionManager;
        this.windowProfileManager = windowProfileManager;
        this.dialogManager = dialogManager;
    }

    public void tryRestoreAppState() {
        boolean profilesExist = Files.exists(Paths.get(PROFILES_DIR, "profile1.state.bin"));
        boolean dialogsStateExists = new File("dialogs.state.bin").exists();
        boolean appStateExists = new File("app.state.bin").exists();

        // --- ВСЕГДА предлагаем профиль если есть хотя бы один! ---
        boolean restored = false;
        if (profilesExist) {
            restored = offerProfileRestore(dialogsStateExists);
        } else if (appStateExists) {
            int res = JOptionPane.showConfirmDialog(
                    frame,
                    messages.getString("restore.prompt.message"),
                    messages.getString("restore.prompt.title"),
                    JOptionPane.YES_NO_OPTION
            );
            if (res == JOptionPane.YES_OPTION) {
                gameSessionManager.restoreAppState();
                // Если карта активна — подгружаем диалоги
                if (gameSessionManager.isGameMapActive() && dialogsStateExists) {
                    dialogManager.restoreDialogStates();
                }
                restored = true;
            }
        }
        // Если не восстановлено — ничего не делаем, игра стартует с нуля
    }

    // Возвращает true если профиль был выбран (и восстановлен), false если пользователь выбрал "не загружать"
    private boolean offerProfileRestore(boolean dialogsStateExists) {
        Path profilesDir = Paths.get(PROFILES_DIR);
        List<Integer> existingProfiles = new ArrayList<>();
        for (int i = 1; i <= MAX_PROFILES; i++) {
            if (Files.exists(profilesDir.resolve("profile" + i + ".state.bin"))) {
                existingProfiles.add(i);
            }
        }
        if (!existingProfiles.isEmpty()) {
            String[] options = new String[existingProfiles.size() + 1];
            for (int i = 0; i < existingProfiles.size(); i++) {
                options[i] = messages.getString("menu.profiles.restore") + " #" + existingProfiles.get(i);
            }
            options[existingProfiles.size()] = messages.getString("profile.restore.none");

            int selected = JOptionPane.showOptionDialog(
                    frame,
                    messages.getString("restore.profile.prompt.message"),
                    messages.getString("restore.profile.prompt.title"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (selected >= 0 && selected < existingProfiles.size()) {
                windowProfileManager.restoreWindowsProfile(existingProfiles.get(selected));
                if (dialogsStateExists) dialogManager.restoreDialogStates();
                return true;
            }
            // если выбрано "Не загружать профиль" или закрыт диалог — ничего не делаем
        }
        return false;
    }
}