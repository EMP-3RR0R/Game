package com.wormfarm.gui.dialog;

import com.wormfarm.settings.AppLocale;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class LoadGameDialog extends JDialog {
    ResourceBundle messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
    private String selectedName = null;
    public LoadGameDialog(JFrame owner, List<String> names) {
        super(owner, ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale()).getString("load.dialog.title"), true);
        setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        names.forEach(model::addElement);
        JList<String> savesList = new JList<>(model);
        savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savesList.setVisibleRowCount(8);
        savesList.setFixedCellWidth(200);
        JScrollPane scrollPane = new JScrollPane(savesList);
        scrollPane.setPreferredSize(new Dimension(220, 160));

        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton ok = new JButton(messages.getString("load.dialog.load"));
        JButton del = new JButton(messages.getString("load.dialog.delete"));
        JButton cancel = new JButton(messages.getString("load.dialog.cancel"));
        btnPanel.add(ok);
        btnPanel.add(del);
        btnPanel.add(cancel);

        ok.addActionListener(e -> {
            String name = savesList.getSelectedValue();
            if (name == null) {
                JOptionPane.showMessageDialog(this, messages.getString("load.dialog.load.not.chosen"));
                return;
            }
            selectedName = name;
            dispose();
        });
        del.addActionListener(e -> {
            String name = savesList.getSelectedValue();
            if (name != null) {
                int res = JOptionPane.showConfirmDialog(this, messages.getString("load.dialog.delete.confirm") + name + "'?", "load.dialog.delete.title", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    model.removeElement(name);
                    com.wormfarm.core.logic.WormSaveManager.deleteSave(name);
                }
            }
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