package com.wormfarm.gui.dialog;

import java.io.Serializable;

public class DialogState implements Serializable {
    public String dialogKey;
    public int x, y, width, height;
    public boolean visible;
    public String extra;

    public DialogState() {}
    public DialogState(String dialogKey) { this.dialogKey = dialogKey; }
}