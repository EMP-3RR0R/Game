package com.wormfarm.gui.base;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class BaseInternalFrame extends JInternalFrame {
    protected ResourceBundle messages;
    public static Locale currentLocale;
    protected boolean allowClose = false;

    // Делегат для кастомного подтверждения закрытия (если нужно)
    protected CustomCloseHandler customCloseHandler;

    public interface CustomCloseHandler {
        /**
         * @return true если окно должно быть закрыто (выбран "Да"), false иначе
         */
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

    /**
     * Централизованная точка подтверждения закрытия окна.
     * Если задан customCloseHandler, он берёт на себя ответственность за диалог.
     */
    protected void confirmClose() {
        if (customCloseHandler != null) {
            boolean shouldClose = customCloseHandler.onCustomClose(this);
            if (shouldClose) {
                allowClose = true;
                dispose();
            }
            // Если пользователь отказал — ничего не делаем, окно не закрывается, второй диалог не появляется.
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