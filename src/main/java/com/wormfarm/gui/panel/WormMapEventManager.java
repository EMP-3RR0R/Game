package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.logic.WormMover;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.minigames.fifteenpuzzle.ui.swing.FifteenPuzzleFrame;
import com.wormfarm.settings.AppLocale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import java.util.ResourceBundle;
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
            Map<EventMarker, Long> recentlyActivated,
            ResourceBundle messages
    ) {
        this.panel = panel;
        this.worm = worm;
        this.mapModel = mapModel;
        this.activeMarkers = activeMarkers;
        this.recentlyActivated = recentlyActivated;
    }

    protected ResourceBundle getMessages() {
        return ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
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
                    if (panel.getGameSessionManager() == null || panel.getGameSessionManager().canActivateEvent()) {
                        activeMarkers.add(marker);
                        SwingUtilities.invokeLater(() -> panel.tryActivateEvent(marker));
                    }
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
        ResourceBundle messages = getMessages();

        if ("puzzle.title".equals(marker.getDescription())) {
            activateFifteenPuzzle(ownerFrame, statsManager, onClose);
        }
    }

    private void activateFifteenPuzzle(JFrame ownerFrame, WormStatsManager statsManager, Runnable onClose) {
        panel.setPaused(true);
        SwingUtilities.invokeLater(() -> {
            JDesktopPane desktopPane = panel.getDesktopPane();
            if (desktopPane == null) {
                JOptionPane.showMessageDialog(ownerFrame, getMessages().getString("error.no_desktop_pane"));
                if (onClose != null) onClose.run();
                return;
            }

            FifteenPuzzleFrame puzzleFrame = (statsManager != null)
                    ? new FifteenPuzzleFrame(statsManager, null)
                    : new FifteenPuzzleFrame();

            setupGameFrame(puzzleFrame, desktopPane, onClose);
        });
    }

    private void setupGameFrame(BaseInternalFrame frame, JDesktopPane desktopPane, Runnable onClose) {
        frame.setClosable(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frame.setCustomCloseHandler(f -> {
            int confirm = JOptionPane.showConfirmDialog(
                    f,
                    getMessages().getString("puzzle.confirm.exit") + "\n" + getMessages().getString("puzzle.progress.lost"),
                    getMessages().getString("puzzle.confirm.exit.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            return confirm == JOptionPane.YES_OPTION;
        });

        frame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                if (onClose != null) onClose.run();
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                panel.repaint();
            }
            @Override
            public void componentResized(ComponentEvent e) {
                panel.repaint();
            }
        });

        desktopPane.add(frame, JLayeredPane.MODAL_LAYER);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (Exception ignored) {}

        centerFrame(frame, desktopPane);
    }

    private void centerFrame(JInternalFrame frame, JDesktopPane desktopPane) {
        int x = (desktopPane.getWidth() - frame.getWidth()) / 2;
        int y = (desktopPane.getHeight() - frame.getHeight()) / 2;
        frame.setLocation(Math.max(0, x), Math.max(0, y));
    }

    public String getEventTitle(EventMarker marker) {
        try {
            return getMessages().getString(marker.getDescription());
        } catch (Exception e) {
            return marker.getDescription();
        }
    }
}