package com.wormfarm.gui.panel;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMarker;
import com.wormfarm.core.model.EventMapModel;
import com.wormfarm.core.logic.WormMover;
import com.wormfarm.core.logic.WormStatsManager;
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
            ResourceBundle messages // оставлено для совместимости сигнатуры
    ) {
        this.panel = panel;
        this.worm = worm;
        this.mapModel = mapModel;
        this.activeMarkers = activeMarkers;
        this.recentlyActivated = recentlyActivated;
    }

    // Utility ― всегда свежий ResourceBundle!
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
        ResourceBundle messages = getMessages();

        if ("puzzle.title".equals(marker.getDescription())) {
            panel.setPaused(true);
            SwingUtilities.invokeLater(() -> {
                JDesktopPane desktopPane = panel.getDesktopPane();
                if (desktopPane == null) {
                    JOptionPane.showMessageDialog(ownerFrame, messages.getString("error.no_desktop_pane"));
                    if (onClose != null) onClose.run();
                    return;
                }

                FifteenPuzzleFrame puzzleFrame = (statsManager != null)
                        ? new FifteenPuzzleFrame(statsManager, null)
                        : new FifteenPuzzleFrame();

                puzzleFrame.setClosable(true);
                puzzleFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                puzzleFrame.setCustomCloseHandler(frame -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            frame,
                            messages.getString("puzzle.confirm.exit") + "\n" + messages.getString("puzzle.progress.lost"),
                            messages.getString("puzzle.confirm.exit.title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );
                    return confirm == JOptionPane.YES_OPTION;
                });

                puzzleFrame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                        if (onClose != null) onClose.run();
                    }
                });

                // repaint карты при перемещении/изменении размера окна пятнашек
                puzzleFrame.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentMoved(ComponentEvent e) {
                        panel.repaint();
                    }
                    @Override
                    public void componentResized(ComponentEvent e) {
                        panel.repaint();
                    }
                });

                desktopPane.add(puzzleFrame, JLayeredPane.MODAL_LAYER);
                puzzleFrame.setVisible(true);
                try {
                    puzzleFrame.setSelected(true);
                } catch (Exception ignored) {}

                int x = (desktopPane.getWidth() - puzzleFrame.getWidth()) / 2;
                int y = (desktopPane.getHeight() - puzzleFrame.getHeight()) / 2;
                puzzleFrame.setLocation(Math.max(0, x), Math.max(0, y));
            });
        }
        // Можно добавить обработку других событий, используя marker.getDescription()
    }

    // Локализованный заголовок события для диалогов
    public String getEventTitle(EventMarker marker) {
        try {
            return getMessages().getString(marker.getDescription());
        } catch (Exception e) {
            return marker.getDescription();
        }
    }
}