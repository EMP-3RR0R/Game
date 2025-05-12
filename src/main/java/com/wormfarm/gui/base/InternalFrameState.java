package com.wormfarm.gui.base;

import java.io.Serializable;

public class InternalFrameState implements Serializable {
    public String windowKey;
    public int x, y, width, height;
    public boolean icon, maximum, visible, selected;
    public int iconX = -1, iconY = -1; // Новое — позиция иконки!
    public String extra;

    public InternalFrameState() {}

    public InternalFrameState(String windowKey) {
        this.windowKey = windowKey;
    }
}