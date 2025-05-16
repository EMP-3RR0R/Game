package com.wormfarm.gui.base;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class BaseInternalFrame extends JInternalFrame {
    protected ResourceBundle messages;
    public static Locale currentLocale;
    protected boolean allowClose = false;
    public abstract String getWindowKey();

    protected CustomCloseHandler customCloseHandler;

    private int lastNormalX = -1, lastNormalY = -1, lastNormalWidth = -1, lastNormalHeight = -1;

    public interface CustomCloseHandler {
        boolean onCustomClose(JInternalFrame frame);
    }

    public void setCustomCloseHandler(CustomCloseHandler handler) {
        this.customCloseHandler = handler;
    }

    static {
        Locale systemLocale = Locale.getDefault();
        if (systemLocale.getLanguage().equals("ru")) {
            currentLocale = new Locale("ru", "RU");
        } else {
            currentLocale = new Locale("en", "EN");
        }
    }

    public BaseInternalFrame(String titleKey, boolean resizable, boolean closable,
                             boolean maximizable, boolean iconifiable) {
        super("", resizable, closable, maximizable, iconifiable);
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", currentLocale);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                confirmClose();
            }
        });
    }

    protected void confirmClose() {
        if (customCloseHandler != null) {
            boolean shouldClose = customCloseHandler.onCustomClose(this);
            if (shouldClose) {
                allowClose = true;
                dispose();
            }
            return;
        }
        if (!allowClose) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    messages.getString("confirm.close.message"),
                    messages.getString("confirm.close.title"),
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                allowClose = true;
                dispose();
            }
        } else {
            dispose();
        }
    }

    public void closeWithoutConfirmation() {
        allowClose = true;
        dispose();
    }

    {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                maybeUpdateNormalBounds();
            }
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                maybeUpdateNormalBounds();
            }
        });
    }

    private void maybeUpdateNormalBounds() {
        if (!isMaximum() && !isIcon()) {
            lastNormalX = getX();
            lastNormalY = getY();
            lastNormalWidth = getWidth();
            lastNormalHeight = getHeight();
        }
    }

    public InternalFrameState exportState() {
        InternalFrameState state = new InternalFrameState(getWindowKey());
        state.x = getX();
        state.y = getY();
        state.width = getWidth();
        state.height = getHeight();
        state.icon = isIcon();
        state.maximum = isMaximum();
        state.visible = isVisible();
        try { state.selected = isSelected(); } catch (Exception e) { state.selected = false; }
        if (state.icon) {
            try {
                Point iconLoc = getDesktopIcon().getLocation();
                state.iconX = iconLoc.x;
                state.iconY = iconLoc.y;
            } catch (Exception ignored) {}
        }
        Rectangle normalBounds = (Rectangle) getClientProperty("JInternalFrame.normalBounds");
        if (normalBounds != null && (state.maximum || state.icon)) {
            boolean looksLikeMaximized = (
                    normalBounds.x == state.x &&
                            normalBounds.y == state.y &&
                            normalBounds.width == state.width &&
                            normalBounds.height == state.height
            );
            if (!looksLikeMaximized) {
                state.normalX = normalBounds.x;
                state.normalY = normalBounds.y;
                state.normalWidth = normalBounds.width;
                state.normalHeight = normalBounds.height;
            } else if (lastNormalWidth > 0 && lastNormalHeight > 0) {
                state.normalX = lastNormalX;
                state.normalY = lastNormalY;
                state.normalWidth = lastNormalWidth;
                state.normalHeight = lastNormalHeight;
            } else {
                state.normalX = -1;
                state.normalY = -1;
                state.normalWidth = -1;
                state.normalHeight = -1;
            }
        } else if (lastNormalWidth > 0 && lastNormalHeight > 0) {
            state.normalX = lastNormalX;
            state.normalY = lastNormalY;
            state.normalWidth = lastNormalWidth;
            state.normalHeight = lastNormalHeight;
        } else {
            state.normalX = state.x;
            state.normalY = state.y;
            state.normalWidth = state.width;
            state.normalHeight = state.height;
        }
        System.out.printf("[DEBUG exportState] %s: x=%d y=%d w=%d h=%d | normalX=%d normalY=%d normalW=%d normalH=%d | max=%b icon=%b selected=%b visible=%b\n",
                getWindowKey(), state.x, state.y, state.width, state.height, state.normalX, state.normalY, state.normalWidth, state.normalHeight,
                state.maximum, state.icon, state.selected, state.visible);
        return state;
    }

    public void importState(InternalFrameState state) {
        System.out.printf("[DEBUG importState] %s: x=%d y=%d w=%d h=%d | normalX=%d normalY=%d normalW=%d normalH=%d | max=%b icon=%b selected=%b visible=%b\n",
                state.windowKey, state.x, state.y, state.width, state.height,
                state.normalX, state.normalY, state.normalWidth, state.normalHeight,
                state.maximum, state.icon, state.selected, state.visible);

        try { setIcon(false); System.out.println("[DEBUG importState] setIcon(false)"); } catch (Exception ignored) {}
        try { setMaximum(false); System.out.println("[DEBUG importState] setMaximum(false)"); } catch (Exception ignored) {}

        int bx = (state.normalX >= 0) ? state.normalX : state.x;
        int by = (state.normalY >= 0) ? state.normalY : state.y;
        int bw = (state.normalWidth > 0) ? state.normalWidth : state.width;
        int bh = (state.normalHeight > 0) ? state.normalHeight : state.height;
        setBounds(bx, by, bw, bh);
        System.out.printf("[DEBUG importState] setBounds(%d, %d, %d, %d)\n", bx, by, bw, bh);

        setVisible(state.visible);
        try { setSelected(state.selected); System.out.println("[DEBUG importState] setSelected(" + state.selected + ")"); } catch (Exception ignored) {}

        try { setMaximum(state.maximum); System.out.println("[DEBUG importState] setMaximum(" + state.maximum + ")"); } catch (Exception ignored) {}
        try { setIcon(state.icon); System.out.println("[DEBUG importState] setIcon(" + state.icon + ")"); } catch (Exception ignored) {}

        if (state.icon && state.iconX >= 0 && state.iconY >= 0) {
            SwingUtilities.invokeLater(() -> {
                try {
                    getDesktopIcon().setLocation(state.iconX, state.iconY);
                    getDesktopIcon().revalidate();
                    getDesktopIcon().repaint();
                    System.out.printf("[DEBUG importState] getDesktopIcon().setLocation(%d, %d)\n", state.iconX, state.iconY);
                } catch (Exception ignored) {}
            });
        }
    }

    public void updateLocale() {
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", currentLocale);
        setTitle(messages.getString(getTitleKey()));
        updateComponents();
    }

    @Override
    public void setMaximum(boolean b) throws java.beans.PropertyVetoException {
        if (b && !isMaximum() && (lastNormalX < 0 || lastNormalY < 0)) {
            lastNormalX = getX();
            lastNormalY = getY();
            lastNormalWidth = getWidth();
            lastNormalHeight = getHeight();
        }
        super.setMaximum(b);
    }

    public static void setAppLocale(Locale locale) {
        currentLocale = locale;
    }

    protected abstract String getTitleKey();
    protected abstract void updateComponents();
}