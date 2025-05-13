package com.wormfarm.gui.state;

import com.wormfarm.gui.dialog.DialogState;
import com.wormfarm.settings.UserSettings;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

import static org.mockito.Mockito.*;

class DialogManagerTest {
    JFrame frame;
    ResourceBundle messages;
    GameSessionManager gsm;
    DialogManager manager;

    @BeforeEach
    void setUp() {
        frame = mock(JFrame.class);
        messages = mock(ResourceBundle.class);
        gsm = mock(GameSessionManager.class);
        manager = new DialogManager(frame, messages, gsm);

        UserSettings us = mock(UserSettings.class);
        when(us.getLanguage()).thenReturn("en");
        when(us.getVolume()).thenReturn(0.5f);
        when(gsm.getSettings()).thenReturn(us);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null && w.isDisplayable()) w.dispose();
        }
        new java.io.File("dialogs.state.bin").delete();
    }

    @Test
    void testAddRemoveDialog() {
        JDialog dlg = new JDialog();
        manager.addDialog(dlg);
        manager.removeDialog(dlg);
        Assertions.assertFalse(manager.openDialogs.contains(dlg));
    }

    @Test
    void testSaveDialogStates_CreatesFile() {
        JDialog dlg = mock(JDialog.class);
        when(dlg.isVisible()).thenReturn(false);
        manager.addDialog(dlg);

        manager.saveDialogStates();
        Assertions.assertTrue(new File("dialogs.state.bin").exists());
    }

    @Test
    void testRestoreDialogStates_CallsRestore() {
        File file = new File("dialogs.state.bin");
        try (var out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
            out.writeObject(new ArrayList<DialogState>());
        } catch (Exception e) {}

        when(gsm.getAvailableSaves()).thenReturn(Collections.emptyList());
        when(gsm.getSettings()).thenReturn(mock(UserSettings.class));
        manager.restoreDialogStates();
    }
}