package com.wormfarm.gui.dialog;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveGameDialogTest {

    @Test
    void testGetSelectedName() {
        JFrame owner = new JFrame();
        SaveGameDialog dialog = new SaveGameDialog(owner, List.of("Save1", "Save2"));

        dialog.setVisible(false); // Simulate dialog closing
        dialog.dispose();

        assertNull(dialog.getSelectedName());
    }

    @Test
    void testSaveWithInput() {
        JFrame owner = new JFrame();
        SaveGameDialog dialog = new SaveGameDialog(owner, List.of("Save1"));

        JTextField nameField = (JTextField) ((JPanel) dialog.getContentPane().getComponent(0)).getComponent(1);
        nameField.setText("New Save");

        JButton okButton = (JButton) ((JPanel) dialog.getContentPane().getComponent(2)).getComponent(0);
        okButton.doClick();

        assertEquals("New Save", dialog.getSelectedName());
    }
}