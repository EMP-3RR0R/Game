package com.wormfarm.gui.base;

import javax.swing.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

public class WindowStateManager {
    public static class InternalFrameState implements Serializable {
        public final String windowKey;
        public final int x, y, width, height;
        public final boolean icon, maximized;

        public InternalFrameState(String windowKey, int x, int y, int width, int height, boolean icon, boolean maximized) {
            this.windowKey = windowKey;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.icon = icon;
            this.maximized = maximized;
        }
    }

    private final List<InternalFrameState> frameStates = new ArrayList<>();
    public List<InternalFrameState> getFrameStates() { return frameStates; }

    public void captureStates(JDesktopPane desktopPane) {
        frameStates.clear();
        Set<String> uniqueKeys = new HashSet<>();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            // Сохраняем все окна, даже свернутые и невидимые!
            String windowKey = (frame instanceof BaseInternalFrame)
                    ? ((BaseInternalFrame) frame).getWindowKey()
                    : frame.getClass().getName();
            if (uniqueKeys.contains(windowKey)) continue;
            uniqueKeys.add(windowKey);

            boolean maximized = false, icon = false;
            try { maximized = frame.isMaximum(); } catch (Exception ignored) {}
            try { icon = frame.isIcon(); } catch (Exception ignored) {}

            frameStates.add(new InternalFrameState(windowKey,
                    frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(),
                    icon, maximized));
        }
    }

    public List<String> restoreStates(JDesktopPane desktopPane, BiFunction<String, InternalFrameState, JInternalFrame> factory) {
        List<String> restored = new ArrayList<>();
        for (InternalFrameState state : frameStates) {
            JInternalFrame frame = factory.apply(state.windowKey, state);
            if (frame == null) continue;

            // --- Явно разрешаем сворачивание и закрытие ---
            frame.setIconifiable(true);
            frame.setClosable(true);

            frame.setBounds(state.x, state.y, state.width, state.height);
            desktopPane.add(frame);
            frame.setVisible(true);

            // --- Swing workaround: iconify fix ---
            if (state.icon) {
                try { frame.setIcon(false); } catch (Exception ignored) {}
                try { frame.setIcon(true); } catch (Exception ignored) {}
            } else {
                try { frame.setIcon(false); } catch (Exception ignored) {}
            }
            try { frame.setMaximum(state.maximized); } catch (Exception ignored) {}

            restored.add(state.windowKey);
        }
        desktopPane.validate();
        desktopPane.repaint();
        return restored;
    }
}