package com.wormfarm.gui.base;

import java.io.Serializable;

public class InternalFrameState implements Serializable {
    public String windowKey;
    public int x, y, width, height;
    public boolean icon, maximum, visible, selected;
    public int iconX = -1, iconY = -1; // Позиция иконки (для свёрнутых окон)
    public String extra;

    public int normalX = -1, normalY = -1, normalWidth = -1, normalHeight = -1;

    public InternalFrameState(String windowKey) {
        this.windowKey = windowKey;
    }
}