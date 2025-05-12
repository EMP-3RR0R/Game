package com.wormfarm.gui.dialog;

import com.wormfarm.settings.AppLocale;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class SaveGameDialog extends JDialog {
    private String selectedName = null;
    ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());

    public SaveGameDialog(JFrame owner, List<String> existingNames) {
        super(owner, ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale()).getString("save.dialog.title"), true);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JLabel lbl = new JLabel(messages.getString("save.dialog.name"));
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
        JButton ok = new JButton(messages.getString("save.dialog.save"));
        JButton cancel = new JButton(messages.getString("save.dialog.cancel"));
        btnPanel.add(ok); btnPanel.add(cancel);

        ok.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, messages.getString("save.dialog.entername"));
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

        setUndecorated(true);
        pack();
        setLocationRelativeTo(owner);
    }

    public DialogState exportState() {
        DialogState state = new DialogState(getDialogKey());
        state.x = getX();
        state.y = getY();
        state.width = getWidth();
        state.height = getHeight();
        state.visible = isVisible();
        state.extra = selectedName; // если нужно
        return state;
    }

    public void importState(DialogState state) {
        setLocation(state.x, state.y);
        setSize(state.width, state.height);
        setVisible(state.visible);
        // selectedName = state.extra; // если нужно восстановить выбранное имя
    }

    public String getSelectedName() {
        return selectedName;
    }
    public String getDialogKey() { return "save.dialog"; }
}