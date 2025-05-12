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

    // Делегат для кастомного подтверждения закрытия (если нужно)
    protected CustomCloseHandler customCloseHandler;

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

    /** Программное закрытие без подтверждения */
    public void closeWithoutConfirmation() {
        allowClose = true;
        dispose();
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
        return state;
    }

    public void importState(InternalFrameState state) {
        setSize(state.width, state.height);
        setLocation(state.x, state.y);
        setVisible(state.visible);
        try { setIcon(state.icon); } catch (Exception ignored) {}
        try { setMaximum(state.maximum); } catch (Exception ignored) {}
        try { setSelected(state.selected); } catch (Exception ignored) {}
        if (state.icon && state.iconX >= 0 && state.iconY >= 0) {
            SwingUtilities.invokeLater(() -> {
                try {
                    getDesktopIcon().setLocation(state.iconX, state.iconY);
                    getDesktopIcon().revalidate();
                    getDesktopIcon().repaint();
                } catch (Exception ignored) {}
            });
        }
    }

    public void updateLocale() {
        messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", currentLocale);
        setTitle(messages.getString(getTitleKey()));
        updateComponents();
    }

    public static void setAppLocale(Locale locale) {
        currentLocale = locale;
    }

    protected abstract String getTitleKey();
    protected abstract void updateComponents();
}