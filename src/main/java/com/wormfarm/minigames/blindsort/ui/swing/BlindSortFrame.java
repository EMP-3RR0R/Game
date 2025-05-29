package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.minigames.blindsort.api.BlindSortGame;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.common.BaseMiniGameFrame;
import com.wormfarm.settings.AppLocale;
import com.wormfarm.settings.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ResourceBundle;

public class BlindSortFrame extends BaseMiniGameFrame {
    public BlindSortLogic game;
    public BlindSortVisualizer visualizer;
    public ResourceBundle messages;
    public Timer uiTimer;
    public JLabel timerLabel;
    public JLabel swapCountLabel;
    public JLabel stageLabel;
    public GameSessionManager gameSessionManager;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUMBERS_COUNT = 5;

    public BlindSortFrame(WormStatsManager wormStatsManager, UserSettings settings,
                          boolean restoreMode, GameSessionManager gameSessionManager) {
        super("blindsort.title", wormStatsManager);
        this.gameSessionManager = gameSessionManager;
        this.game = new BlindSortLogic(NUMBERS_COUNT);
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        this.visualizer = new BlindSortVisualizer(game, WIDTH, HEIGHT);
        BlindSortController controller = new BlindSortController(game, visualizer, this, wormStatsManager, messages);
        initUI();
        if (!restoreMode) {
            startGame();
        }
        SwingUtilities.invokeLater(() -> visualizer.requestFocusInWindow());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                try {
                    setMaximum(true);
                } catch (java.beans.PropertyVetoException ignored) {
                }
            }
        });
        setCustomCloseHandler(frame -> JOptionPane.showConfirmDialog(frame,
                messages.getString("blindsort.message.confirm_exit"),
                messages.getString("blindsort.title.confirm_exit"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
    }

    public void initUI() {
        setTitle(messages.getString("blindsort.title"));
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(visualizer, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        controlPanel.setBackground(new Color(60, 63, 65));

        JButton nextStageButton = new JButton(messages.getString("blindsort.next_stage"));
        nextStageButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextStageButton.setForeground(Color.WHITE);
        nextStageButton.setBackground(new Color(75, 110, 170));
        nextStageButton.setFocusPainted(false);
        nextStageButton.addActionListener(e -> {
            if (game.isGameActive()) {
                game.nextStage();
            }
        });
        controlPanel.add(nextStageButton);

        JButton resetButton = new JButton(messages.getString("blindsort.reset"));
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(170, 75, 75));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> {
            game.resetGame();
            updateInfo();
        });
        controlPanel.add(resetButton);

        timerLabel = new JLabel(messages.getString("blindsort.time") + ": 00:00");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timerLabel.setForeground(Color.WHITE);
        controlPanel.add(timerLabel);

        swapCountLabel = new JLabel(messages.getString("blindsort.moves") + ": 0");
        swapCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        swapCountLabel.setForeground(Color.WHITE);
        controlPanel.add(swapCountLabel);

        stageLabel = new JLabel(messages.getString("blindsort.stage") + ": 1");
        stageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        stageLabel.setForeground(Color.WHITE);
        controlPanel.add(stageLabel);

        contentPane.add(controlPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);

        uiTimer = new Timer(1000, e -> updateInfo());
        uiTimer.start();

        pack();
    }

    @Override
    public void startGame() {
        game.resetGame();
        updateInfo();
    }

    public void updateInfo() {
        long elapsedTime = game.getElapsedTime() / 1000;
        long minutes = elapsedTime / 60;
        long seconds = elapsedTime % 60;
        timerLabel.setText(String.format("%s: %02d:%02d", messages.getString("blindsort.time"), minutes, seconds));
        swapCountLabel.setText(messages.getString("blindsort.moves") + ": " + game.getSwapCount());
        stageLabel.setText(game.isFinalStage() ? messages.getString("blindsort.stage") + ": " + messages.getString("blindsort.stage_final") :
                messages.getString("blindsort.stage") + ": " + game.getVisibleDigits());
        visualizer.repaint();
        if (!game.isGameActive() && uiTimer != null && uiTimer.isRunning()) {
            uiTimer.stop();
        }
    }

    @Override
    public void setCustomCloseHandler(BaseInternalFrame.CustomCloseHandler handler) {
        super.setCustomCloseHandler(handler);
    }

    @Override
    public String getTitleKey() {
        return "blindsort.title";
    }

    @Override
    public String getWindowKey() {
        return "blindsort.title";
    }

    @Override
    public void pauseGame() {
        game.pauseGame();
    }

    @Override
    public void resumeGame() {
        game.resumeGame();
    }

    @Override
    public void endGame() {
        if (uiTimer != null) {
            uiTimer.stop();
        }
        game.stopAI();
        dispose();
    }

    @Override
    protected void updateComponents() {
    }

    @Override
    public void dispose() {
        if (uiTimer != null) {
            uiTimer.stop();
        }
        game.stopAI();
        super.dispose();
    }
}