/*package com.wormfarm.gui.frame;

import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.gui.panel.WormMapPanel;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.EventMapModel;

import java.awt.*;
import javax.swing.JFrame;

public class WormGameWindow extends BaseInternalFrame {
    public WormGameWindow(JFrame ownerFrame) {
        super("game.window.title", false, false, false, false);

        int startX = 100;
        int startY = 100;

        WormState worm = new WormState(startX, startY, 0);
        EventMapModel mapModel = new EventMapModel();
        WormMapPanel visualizer = new WormMapPanel(worm, mapModel, ownerFrame);

        setLayout(new BorderLayout());
        getContentPane().add(visualizer, BorderLayout.CENTER);

        setPreferredSize(new Dimension(WormMapPanel.FIELD_WIDTH, WormMapPanel.FIELD_HEIGHT));
        setMinimumSize(new Dimension(WormMapPanel.FIELD_WIDTH, WormMapPanel.FIELD_HEIGHT));
        setMaximumSize(new Dimension(WormMapPanel.FIELD_WIDTH, WormMapPanel.FIELD_HEIGHT));
        setSize(WormMapPanel.FIELD_WIDTH, WormMapPanel.FIELD_HEIGHT);
        pack();
    }

    @Override
    protected String getTitleKey() {
        return "game.window.title";
    }

    @Override
    protected void updateComponents() {
    }
}*/