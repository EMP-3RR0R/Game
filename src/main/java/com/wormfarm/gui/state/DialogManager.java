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

    private final DialogStateManager dialogStateManager = new DialogStateManager();
    private final List<JDialog> openDialogs = new ArrayList<>();

    public DialogManager(JFrame frame, ResourceBundle messages, GameSessionManager gameSessionManager) {
        this.frame = frame;
        this.messages = messages;
        this.gameSessionManager = gameSessionManager;
    }

    public void addDialog(JDialog dialog) {
        openDialogs.add(dialog);
    }

    public void removeDialog(JDialog dialog) {
        openDialogs.remove(dialog);
    }

    /** Сохраняет состояния всех открытых диалогов в файл "dialogs.state.bin". */
    public void saveDialogStates() {
        dialogStateManager.captureStates(openDialogs);
        try {
            dialogStateManager.saveToFile(new File("dialogs.state.bin"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Восстанавливает состояния диалогов из файла "dialogs.state.bin". */
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
                        SettingsDialog settingsDlg = new SettingsDialog(owner, null, gameSessionManager.getSettings());
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

    // Методы для удобного открытия диалогов с учётом списка openDialogs
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
        SettingsDialog dlg = new SettingsDialog(frame, null, gameSessionManager.getSettings());
        addDialog(dlg);
        dlg.setVisible(true);
        removeDialog(dlg);
    }
}