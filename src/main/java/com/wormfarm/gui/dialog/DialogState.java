package com.wormfarm.gui.dialog;

import java.io.Serializable;

public class DialogState implements Serializable {
    public String dialogKey; // уникальный ключ (например, "save.dialog", "load.dialog", "settings.dialog")
    public int x, y, width, height;
    public boolean visible;
    public String extra; // дополнительные параметры (выбранное имя и т.п.)

    public DialogState() {}
    public DialogState(String dialogKey) { this.dialogKey = dialogKey; }
}