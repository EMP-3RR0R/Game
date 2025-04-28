package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.logic.WormMover;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.minigames.fifteenpuzzle.ui.swing.FifteenPuzzleFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;

public class WormMapEventManager {
    private final WormMapPanel panel;
    private final WormState worm;
    private final EventMapModel mapModel;
    private final Set<EventMarker> activeMarkers;
    private final Map<EventMarker, Long> recentlyActivated;

    public WormMapEventManager(
            WormMapPanel panel,
            WormState worm,
            EventMapModel mapModel,
            Set<EventMarker> activeMarkers,
            Map<EventMarker, Long> recentlyActivated
    ) {
        this.panel = panel;
        this.worm = worm;
        this.mapModel = mapModel;
        this.activeMarkers = activeMarkers;
        this.recentlyActivated = recentlyActivated;
    }

    public void updateWormAndEvents(int targetX, int targetY, boolean paused) {
        WormMover.moveWorm(worm, targetX, targetY, 10, WormMapPanel.FIELD_WIDTH, WormMapPanel.FIELD_HEIGHT);
        if (paused) return;

        long now = System.currentTimeMillis();

        for (EventMarker marker : mapModel.getMarkers()) {
            double dist = WormMover.distance(worm.getX(), worm.getY(), marker.getX(), marker.getY());
            boolean inside = dist < 40;

            if (inside) {
                if (!activeMarkers.contains(marker) &&
                        (!recentlyActivated.containsKey(marker) ||
                                now - recentlyActivated.get(marker) > 5000)) {
                    activeMarkers.add(marker);
                    SwingUtilities.invokeLater(() -> panel.tryActivateEvent(marker));
                }
            } else {
                if (activeMarkers.contains(marker)) {
                    activeMarkers.remove(marker);
                    recentlyActivated.put(marker, now);
                }
            }

            recentlyActivated.entrySet().removeIf(e -> now - e.getValue() > 5000 && !activeMarkers.contains(e.getKey()));
        }
    }

    public void activateEvent(EventMarker marker, JFrame ownerFrame, WormStatsManager statsManager, Runnable onClose) {
        if (marker.getDescription().equals("Пятнашки")) {
            panel.setPaused(true);
            SwingUtilities.invokeLater(() -> {
                FifteenPuzzleFrame puzzleFrame = (statsManager != null)
                        ? new FifteenPuzzleFrame(statsManager)
                        : new FifteenPuzzleFrame();
                JDialog dialog = new JDialog(ownerFrame, "Пятнашки", true);
                puzzleFrame.setParentDialog(dialog);

                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.setContentPane(puzzleFrame.getContentPane());
                dialog.setSize(puzzleFrame.getPreferredSize());
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(ownerFrame);

                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(
                                dialog,
                                "Вы уверены, что хотите выйти из пятнашек?\nПрогресс будет потерян.",
                                "Подтвердите выход",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            dialog.dispose();
                        }
                    }
                    @Override
                    public void windowClosed(WindowEvent e) {
                        if (onClose != null) onClose.run();
                    }
                });

                dialog.setVisible(true);
            });
        }
        // Можно добавить обработку других событий
    }
}