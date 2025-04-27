package com.wormfarm.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoadGameDialog extends JDialog {
    private String selectedName = null;
    public LoadGameDialog(JFrame owner, List<String> names) {
        super(owner, "Загрузить игру", true);
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
        JButton ok = new JButton("Загрузить");
        JButton del = new JButton("Удалить");
        JButton cancel = new JButton("Отмена");
        btnPanel.add(ok);
        btnPanel.add(del);
        btnPanel.add(cancel);

        ok.addActionListener(e -> {
            String name = savesList.getSelectedValue();
            if (name == null) {
                JOptionPane.showMessageDialog(this, "Выберите сохранение!");
                return;
            }
            selectedName = name;
            dispose();
        });
        del.addActionListener(e -> {
            String name = savesList.getSelectedValue();
            if (name != null) {
                int res = JOptionPane.showConfirmDialog(this, "Удалить сохранение '" + name + "'?", "Удаление", JOptionPane.YES_NO_OPTION);
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