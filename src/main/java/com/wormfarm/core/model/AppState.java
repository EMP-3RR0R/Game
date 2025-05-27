package com.wormfarm.core.model;

import java.io.Serializable;

public class AppState implements Serializable {
    public enum Mode { MAIN_MENU, GAME_MAP }
    public Mode mode;
    public String lastSaveName;

    public AppState() {}
    public AppState(Mode mode, String lastSaveName) {
        this.mode = mode;
        this.lastSaveName = lastSaveName;
    }
}