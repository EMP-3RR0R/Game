package com.wormfarm.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SaveGameDialog extends JDialog {
    private String selectedName = null;
    public SaveGameDialog(JFrame owner, List<String> existingNames) {
        super(owner, "save.dialog.title", true);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JLabel lbl = new JLabel("save.dialog.name");
        JTextField nameField = new JTextField();
        inputPanel.add(lbl, BorderLayout.WEST);
        inputPanel.add(nameField, BorderLayout.CENTER);

        DefaultListModel<String> model = new DefaultListModel<>();
        existingNames.forEach(model::addElement);
        JList<String> savesList = new JList<>(model);
        savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savesList.setVisibleRowCount(6);
        savesList.setFixedCellWidth(180);
        JScrollPane scrollPane = new JScrollPane(savesList);
        scrollPane.setPreferredSize(new Dimension(200, 120));

        savesList.addListSelectionListener(e -> {
            String sel = savesList.getSelectedValue();
            if (sel != null) nameField.setText(sel);
        });

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton ok = new JButton("save.dialog.save");
        JButton cancel = new JButton("save.dialog.cancel");
        btnPanel.add(ok); btnPanel.add(cancel);

        ok.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "save.dialog.entername");
                return;
            }
            selectedName = name;
            dispose();
        });
        cancel.addActionListener(e -> {
            selectedName = null;
            dispose();
        });

        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public String getSelectedName() {
        return selectedName;
    }
}