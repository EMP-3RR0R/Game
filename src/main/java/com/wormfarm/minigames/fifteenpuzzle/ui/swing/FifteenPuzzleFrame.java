package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import com.wormfarm.minigames.common.BaseMiniGameFrame;
import com.wormfarm.minigames.fifteenpuzzle.logic.ClassicFifteenPuzzleLogic;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.core.model.WormStats;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.UserSettings;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.gui.panel.WormMapPanel;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class FifteenPuzzleFrame extends BaseMiniGameFrame {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;

    private final ClassicFifteenPuzzleLogic logic;
    private final FifteenPuzzleVisualizer visualizer;
    private final FifteenPuzzleController controller;

    private final JLabel timerLabel = new JLabel();
    private final JLabel movesLabel = new JLabel();
    private final Timer uiTimer;

    private final UserSettings settings;
    private ResourceBundle messages;

    private final boolean restoreMode;
    private final GameSessionManager gameSessionManager;

    public FifteenPuzzleFrame(GameSessionManager gameSessionManager) {
        this(new WormStatsManager(new WormStats(0)), null, true, gameSessionManager);
    }

    public FifteenPuzzleFrame(WormStatsManager wormStatsManager, UserSettings settings, boolean restoreMode, GameSessionManager gameSessionManager) {
        super("puzzle.title", wormStatsManager);

        setIconifiable(true);
        setClosable(true);

        this.settings = settings;
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        this.restoreMode = restoreMode;
        this.gameSessionManager = gameSessionManager;

        logic = new ClassicFifteenPuzzleLogic(SIZE);
        visualizer = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, logic);
        controller = new FifteenPuzzleController(logic, visualizer, this, wormStatsManager, this);

        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        movesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(timerLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(movesLabel);
        add(infoPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton resetButton = new JButton(messages.getString("puzzle.reset"));
        JButton solveButton = new JButton(messages.getString("puzzle.autosolve"));

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
        setResizable(false);

        visualizer.setBoard(logic.getBoardCopy());
        visualizer.resetPuzzleImage();

        uiTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateInfo();
            }
        });
        uiTimer.start();

        updateInfo();

        if (!restoreMode) {
            startGame();
        }

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                if (gameSessionManager != null) {
                    gameSessionManager.onResumableWindowClosed();
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) mapPanel.repaint();
                }
            }
            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
                visualizer.repaint();
            }
            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                visualizer.repaint();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (gameSessionManager != null) {
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) mapPanel.repaint();
                }
            }
            @Override
            public void componentResized(ComponentEvent e) {
                if (gameSessionManager != null) {
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) mapPanel.repaint();
                }
            }
        });
    }

    public FifteenPuzzleFrame(WormStatsManager wormStatsManager, UserSettings settings) {
        this(wormStatsManager, settings, false, null);
    }

    public FifteenPuzzleFrame(boolean restoreMode) {
        this(new WormStatsManager(new WormStats(0)), null, restoreMode, null);
    }

    public FifteenPuzzleFrame() {
        this(false);
    }

    private void updateInfo() {
        long ms = logic.getElapsedTimeMillis();
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        timerLabel.setText(MessageFormat.format("{0}: {1}:{2}", messages.getString("puzzle.time"), String.format("%02d", min), String.format("%02d", sec)));
        movesLabel.setText(MessageFormat.format("{0}: {1}", messages.getString("puzzle.moves"), logic.getMoveCount()));
    }

    @Override
    protected String getTitleKey() {
        return "puzzle.title";
    }

    @Override
    protected void updateComponents() {
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        updateInfo();
    }

    @Override
    public void dispose() {
        uiTimer.stop();
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

    @Override
    public String getWindowKey() {
        return "minigame.fifteen.puzzle";
    }

    public ResourceBundle getMessages() {
        return messages;
    }
}