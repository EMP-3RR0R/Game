package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.gui.base.BaseInternalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FifteenPuzzleFrame extends BaseInternalFrame {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;

    private JDialog parentDialog;

    private final ClassicFifteenPuzzleLogic logic;
    private final FifteenPuzzleVisualizer visualizer;
    private final FifteenPuzzleController controller;

    private final JLabel timerLabel = new JLabel("Время: 00:00");
    private final JLabel movesLabel = new JLabel("Ходы: 0");
    private final Timer uiTimer;

    public FifteenPuzzleFrame() {
        super("puzzle.title", true, true, true, true);

        setLayout(new BorderLayout());

        logic = new ClassicFifteenPuzzleLogic(SIZE);
        visualizer = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, logic);
        controller = new FifteenPuzzleController(logic, visualizer, this);

        // Панель с таймером и ходами
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        movesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(timerLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(movesLabel);
        add(infoPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton resetButton = new JButton("Сброс");
        JButton solveButton = new JButton("Решить автоматически");

        resetButton.addActionListener(e -> {
            logic.resetBoard();
            visualizer.resetPuzzleImage(); // Меняем спрайт при сбросе
            visualizer.setBoard(logic.getBoardCopy());
            visualizer.repaint();
            updateInfo();
        });

        solveButton.addActionListener(e -> {
            logic.solvePuzzle();
            visualizer.setBoard(logic.getBoardCopy());
            visualizer.repaint();
            updateInfo();
        });

        buttonPanel.add(resetButton);
        buttonPanel.add(solveButton);

        add(visualizer, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 120);
        setPreferredSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 120));
        setMinimumSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 120));
        setMaximumSize(new Dimension(SIZE * TILE_SIZE + 20, SIZE * TILE_SIZE + 120));
        setResizable(false); // Запретить изменение размера окна

        visualizer.setBoard(logic.getBoardCopy());
        visualizer.resetPuzzleImage(); // Первый спрайт при старте

        uiTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateInfo();
            }
        });
        uiTimer.start();
    }

    private void updateInfo() {
        long ms = logic.getElapsedTimeMillis();
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        timerLabel.setText(String.format("Время: %02d:%02d", min, sec));
        movesLabel.setText("Ходы: " + logic.getMoveCount());
    }

    // Для связи с диалогом
    public void setParentDialog(JDialog dialog) {
        this.parentDialog = dialog;
    }
    public JDialog getParentDialog() {
        return parentDialog;
    }

    @Override
    protected String getTitleKey() {
        return "puzzle.title";
    }

    @Override
    protected void updateComponents() {
        // Обновление локализации и других компонентов, если потребуется
    }

    @Override
    public void dispose() {
        super.dispose();
        visualizer.clearSprites(); // Очищаем ресурсы спрайтов при закрытии
    }
}