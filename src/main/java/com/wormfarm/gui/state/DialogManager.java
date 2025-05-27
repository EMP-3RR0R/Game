package com.wormfarm.gui.state;

import com.wormfarm.gui.dialog.DialogStateManager;
import com.wormfarm.gui.dialog.DialogState;
import com.wormfarm.gui.dialog.LoadGameDialog;
import com.wormfarm.gui.dialog.SaveGameDialog;
import com.wormfarm.settings.SettingsDialog;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DialogManager {

    private final JFrame frame;
    private final ResourceBundle messages;
    private final GameSessionManager gameSessionManager;
    private final Runnable onLanguageChange;

    private final DialogStateManager dialogStateManager = new DialogStateManager();
    final List<JDialog> openDialogs = new ArrayList<>();

    public DialogManager(JFrame frame, ResourceBundle messages, GameSessionManager gameSessionManager, Runnable onLanguageChange) {
        this.frame = frame;
        this.messages = messages;
        this.gameSessionManager = gameSessionManager;
        this.onLanguageChange = onLanguageChange;
    }

    public void addDialog(JDialog dialog) {
        openDialogs.add(dialog);
    }

    public void removeDialog(JDialog dialog) {
        openDialogs.remove(dialog);
    }

    public void saveDialogStates() {
        dialogStateManager.captureStates(openDialogs);
        try {
            dialogStateManager.saveToFile(new File("dialogs.state.bin"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreDialogStates() {
        try {
            dialogStateManager.loadFromFile(new File("dialogs.state.bin"));
            dialogStateManager.restoreStates(frame, (dialogKey, state, owner) -> {
                switch (dialogKey) {
                    case "load.dialog":
                        LoadGameDialog loadDlg = new LoadGameDialog(owner, gameSessionManager.getAvailableSaves());
                        loadDlg.importState(state);
                        addDialog(loadDlg);
                        return loadDlg;
                    case "save.dialog":
                        SaveGameDialog saveDlg = new SaveGameDialog(owner, gameSessionManager.getAvailableSaves());
                        saveDlg.importState(state);
                        addDialog(saveDlg);
                        return saveDlg;
                    case "settings.dialog":
                        SettingsDialog settingsDlg = new SettingsDialog(owner, onLanguageChange, gameSessionManager.getSettings()); // <--- передаём колбэк!
                        settingsDlg.importState(state);
                        addDialog(settingsDlg);
                        return settingsDlg;
                    default:
                        return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadGameDialog() {
        LoadGameDialog dlg = new LoadGameDialog(frame, gameSessionManager.getAvailableSaves());
        addDialog(dlg);
        dlg.setVisible(true);
        removeDialog(dlg);
    }

    public void showSaveGameDialog() {
        SaveGameDialog dlg = new SaveGameDialog(frame, gameSessionManager.getAvailableSaves());
        addDialog(dlg);
        dlg.setVisible(true);
        removeDialog(dlg);
    }

    public void showSettingsDialog() {
        SettingsDialog dlg = new SettingsDialog(frame, onLanguageChange, gameSessionManager.getSettings()); // <--- передаём колбэк!
        addDialog(dlg);
        dlg.setVisible(true);
        removeDialog(dlg);
    }
}