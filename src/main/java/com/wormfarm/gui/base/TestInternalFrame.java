package com.wormfarm.gui.base;

public class TestInternalFrame extends BaseInternalFrame {
    private final String key;

    public TestInternalFrame(String key) {
        super(key, true, true, true, true);
        this.key = key;
        setTitle(key);
        setSize(300, 200);
        setLocation(100, 100);
    }

    @Override
    public String getWindowKey() {
        return key;
    }

    @Override
    protected String getTitleKey() {
        return key;
    }

    @Override
    protected void updateComponents() {
    }
}