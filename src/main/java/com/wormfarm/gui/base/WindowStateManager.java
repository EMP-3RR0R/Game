package com.wormfarm.gui.base;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

public class WindowStateManager {
    private final List<InternalFrameState> frameStates = new ArrayList<>();
    public List<InternalFrameState> getFrameStates() { return frameStates; }

    public void captureStates(JDesktopPane desktopPane) {
        frameStates.clear();
        Set<String> uniqueKeys = new HashSet<>();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            // ВАЖНО: Сохраняем только видимые окна!
            if (!frame.isVisible()) continue;

            String windowKey = (frame instanceof BaseInternalFrame)
                    ? ((BaseInternalFrame) frame).getWindowKey()
                    : frame.getClass().getName();
            if (uniqueKeys.contains(windowKey)) continue;
            uniqueKeys.add(windowKey);

            InternalFrameState state;
            if (frame instanceof BaseInternalFrame) {
                state = ((BaseInternalFrame) frame).exportState();
            } else {
                state = new InternalFrameState(windowKey);
                state.x = frame.getX();
                state.y = frame.getY();
                state.width = frame.getWidth();
                state.height = frame.getHeight();
                state.icon = frame.isIcon();
                state.maximum = frame.isMaximum();
                state.visible = frame.isVisible();
                try { state.selected = frame.isSelected(); } catch (Exception e) { state.selected = false; }
                if (state.icon) {
                    try {
                        Point p = frame.getDesktopIcon().getLocation();
                        state.iconX = p.x;
                        state.iconY = p.y;
                    } catch (Exception ignored) {}
                }
            }
            frameStates.add(state);
        }
    }

    public List<String> restoreStates(JDesktopPane desktopPane, BiFunction<String, InternalFrameState, JInternalFrame> factory) {
        List<String> restored = new ArrayList<>();
        for (InternalFrameState state : frameStates) {
            JInternalFrame frame = factory.apply(state.windowKey, state);
            if (frame == null) continue;

            frame.setIconifiable(true);
            frame.setClosable(true);

            frame.setBounds(state.x, state.y, state.width, state.height);
            desktopPane.add(frame, JLayeredPane.MODAL_LAYER);
            frame.setVisible(state.visible);

            if (frame instanceof BaseInternalFrame) {
                ((BaseInternalFrame) frame).importState(state);
            } else {
                try { frame.setIcon(state.icon); } catch (Exception ignored) {}
                try { frame.setMaximum(state.maximum); } catch (Exception ignored) {}
                try { frame.setSelected(state.selected); } catch (Exception ignored) {}
                // --- Восстанавливаем положение иконки
                if (state.icon && state.iconX >= 0 && state.iconY >= 0) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            frame.getDesktopIcon().setLocation(state.iconX, state.iconY);
                            frame.getDesktopIcon().revalidate();
                            frame.getDesktopIcon().repaint();
                        } catch (Exception ignored) {}
                    });
                }
            }
            restored.add(state.windowKey);
        }
        desktopPane.validate();
        desktopPane.repaint();
        return restored;
    }
}