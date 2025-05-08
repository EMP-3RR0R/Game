package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.common.BaseMiniGameFrame;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.WormStats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class FifteenPuzzleFrame extends BaseMiniGameFrame {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;

    private JDialog parentDialog;

    private final ClassicFifteenPuzzleLogic logic;
    private final FifteenPuzzleVisualizer visualizer;
    private final FifteenPuzzleController controller;

    private final JLabel timerLabel = new JLabel("Время: 00:00");
    private final JLabel movesLabel = new JLabel("Ходы: 0");
    private final Timer uiTimer;

    // --- Новое поле для менеджера статистики ---
    private final WormStatsManager wormStatsManager;

    // Новый конструктор, передающий WormStatsManager извне
    public FifteenPuzzleFrame(WormStatsManager wormStatsManager) {
        super("puzzle.title", wormStatsManager);
        System.out.println("here");

        this.wormStatsManager = wormStatsManager;

        logic = new ClassicFifteenPuzzleLogic(SIZE);
        visualizer = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, logic);
        controller = new FifteenPuzzleController(logic, visualizer, this, wormStatsManager);

        // --- Гарантированная обработка ESC, даже если фокус не на нужном компоненте ---
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.out.println("dkdf");
                    if (controller.isPausedGame()) {
                        controller.resumeGame();
                    } else {
                        controller.pauseGame();
                        showPauseMenu();
                    }
                }
                return false;
            }
        });



        setLayout(new BorderLayout());

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
            visualizer.resetPuzzleImage();
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
        setResizable(false); // Prevent resizing

        visualizer.setBoard(logic.getBoardCopy());
        visualizer.resetPuzzleImage(); // First sprite when starting

        uiTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateInfo();
            }
        });
        uiTimer.start();
    }

    public FifteenPuzzleFrame() {
        this(new WormStatsManager(new WormStats(0)));
    }

    private void updateInfo() {
        long ms = logic.getElapsedTimeMillis();
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        timerLabel.setText(String.format("Время: %02d:%02d", min, sec));
        movesLabel.setText("Ходы: " + logic.getMoveCount());
    }

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
        // Update localization and other components if needed
    }

    @Override
    public void dispose() {
        super.dispose();
        visualizer.clearSprites();
    }

    @Override
    public void startGame() {
        logic.resetBoard();
        visualizer.setBoard(logic.getBoardCopy());
        visualizer.resetPuzzleImage();
        visualizer.repaint();
        uiTimer.start();
    }

    @Override
    public void pauseGame() {
        uiTimer.stop();
        logic.pauseGame();
    }

    @Override
    public void resumeGame() {
        uiTimer.start();
        logic.resumeGame();
    }
    @Override
    public void endGame() {
        uiTimer.stop();
        visualizer.clearSprites();
    }
}
