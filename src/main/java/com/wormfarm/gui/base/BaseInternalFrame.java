package com.wormfarm.gui.base;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class BaseInternalFrame extends JInternalFrame {
    protected ResourceBundle messages;
    public static Locale currentLocale;

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
        updateLocale();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                confirmClose();
            }
        });
    }

    protected void confirmClose() {
        int result = JOptionPane.showConfirmDialog(
                this,
                messages.getString("confirm.close.message"),
                messages.getString("confirm.close.title"),
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
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