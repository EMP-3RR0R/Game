package com.wormfarm.gui.dialog;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class DialogStateManager {
    private final List<DialogState> dialogStates = new ArrayList<>();

    public void captureStates(List<JDialog> dialogs) {
        dialogStates.clear();
        for (JDialog dialog : dialogs) {
            if (dialog instanceof SaveGameDialog) {
                dialogStates.add(((SaveGameDialog) dialog).exportState());
            } else if (dialog instanceof LoadGameDialog) {
                dialogStates.add(((LoadGameDialog) dialog).exportState());
            } else if (dialog instanceof com.wormfarm.settings.SettingsDialog) {
                dialogStates.add(((com.wormfarm.settings.SettingsDialog) dialog).exportState());
            }
        }
    }

    public void restoreStates(JFrame owner, DialogFactory factory) {
        for (DialogState state : dialogStates) {
            JDialog dialog = factory.createDialog(state.dialogKey, state, owner);
            if (dialog == null) continue;
            if (dialog instanceof SaveGameDialog) {
                ((SaveGameDialog) dialog).importState(state);
            } else if (dialog instanceof LoadGameDialog) {
                ((LoadGameDialog) dialog).importState(state);
            } else if (dialog instanceof com.wormfarm.settings.SettingsDialog) {
                ((com.wormfarm.settings.SettingsDialog) dialog).importState(state);
            }
            dialog.setVisible(state.visible);
        }
    }

    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(dialogStates);
        }
    }

    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<?> loaded = (List<?>) in.readObject();
            dialogStates.clear();
            for (Object obj : loaded) {
                if (obj instanceof DialogState)
                    dialogStates.add((DialogState) obj);
            }
        }
    }

    public interface DialogFactory {
        JDialog createDialog(String dialogKey, DialogState state, JFrame owner);
    }

    public List<DialogState> getDialogStates() {
        return dialogStates;
    }
}