package com.wormfarm.farm.market;

import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.farm.FarmController;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.plant.PlantInstance;
import com.wormfarm.core.logic.WormStatsManager;
import com.wormfarm.gui.state.GameSessionManager;
import com.wormfarm.gui.panel.WormMapPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.ArrayList;

public class FarmMarketPanel extends BaseInternalFrame {
    private final JPanel itemsPanel;
    private final FarmController farmController;
    private final WormStatsManager statsManager;
    private final List<MarketItem> marketItems = new ArrayList<>();
    private final JLabel coinsLabel;
    private final GameSessionManager gameSessionManager;

    public FarmMarketPanel(FarmController farmController, WormStatsManager statsManager, GameSessionManager gameSessionManager) {
        super("farm.market.title", false, true, false, false);
        this.farmController = farmController;
        this.statsManager = statsManager;
        this.gameSessionManager = gameSessionManager;

        setTitle(messages.getString("farm.market.title"));
        setSize(700, 320);
        setPreferredSize(new Dimension(700, 320));
        setLayout(new BorderLayout());
        setClosable(true);

        // Визуальная прозрачность для панели и базовых компонентов
        setOpaque(false);
        setBackground(new Color(255, 255, 255, 220)); // полупрозрачный белый

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        topPanel.setOpaque(false); // прозрачный фон
        coinsLabel = new JLabel();
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        updateCoinsLabel();
        topPanel.add(coinsLabel);
        add(topPanel, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false); // прозрачный фон
        itemsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 12));
        JScrollPane scrollPane = new JScrollPane(itemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadMarketItems();
        updateComponents();

        // --- СТАВИМ ПАУЗУ ---
        if (gameSessionManager != null && gameSessionManager.getCurrentMapPanel() != null) {
            gameSessionManager.getCurrentMapPanel().pauseGame();
        }

        // --- ГЛАВНОЕ! --- Добавляем слушатель для полного repaint карты при закрытии, перемещении и ресайзе окна
        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                if (gameSessionManager != null) {
                    gameSessionManager.onResumableWindowClosed();
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) mapPanel.repaint(); // исправляет "мелькание" червяка
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                WormMapPanel mapPanel = (gameSessionManager != null) ? gameSessionManager.getCurrentMapPanel() : null;
                if (mapPanel != null) mapPanel.repaint();
            }
            @Override
            public void componentResized(ComponentEvent e) {
                WormMapPanel mapPanel = (gameSessionManager != null) ? gameSessionManager.getCurrentMapPanel() : null;
                if (mapPanel != null) mapPanel.repaint();
            }
        });
    }

    private void updateCoinsLabel() {
        coinsLabel.setText(
                messages.getString("market.balance") + ": " +
                        (statsManager != null ? statsManager.getCoins() : 0) + " WC"
        );
    }

    @Override
    public String getWindowKey() {
        return "farm.market";
    }

    @Override
    protected String getTitleKey() {
        return "farm.market.title";
    }

    @Override
    protected void updateComponents() {
        itemsPanel.removeAll();
        for (MarketItem item : marketItems) {
            itemsPanel.add(createMarketCell(item));
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
        updateCoinsLabel();
    }

    private void loadMarketItems() {
        marketItems.clear();
        marketItems.add(new MarketItem(
                messages.getString("market.dungbeetle.name"),
                MarketItem.Type.INSECT,
                30,
                messages.getString("market.dungbeetle.effect"),
                (panel) -> buyDungBeetle(panel)
        ));
        marketItems.add(new MarketItem(
                messages.getString("market.plant.name"),
                MarketItem.Type.PLANT,
                10,
                messages.getString("market.plant.effect"),
                (panel) -> buyPlant(panel)
        ));
    }

    private JPanel createMarketCell(MarketItem item) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setPreferredSize(new Dimension(260, 180));
        cell.setMaximumSize(new Dimension(260, 180));
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(nameLabel);

        JLabel typeLabel = new JLabel(messages.getString("market.type." + item.type.name().toLowerCase()), SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        typeLabel.setForeground(Color.DARK_GRAY);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(typeLabel);

        JTextArea effectArea = new JTextArea(item.effect);
        effectArea.setOpaque(false);
        effectArea.setFont(new Font("Arial", Font.PLAIN, 14));
        effectArea.setLineWrap(true);
        effectArea.setWrapStyleWord(true);
        effectArea.setEditable(false);
        effectArea.setFocusable(false);
        effectArea.setBackground(new Color(0,0,0,0));
        effectArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        effectArea.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        effectArea.setMaximumSize(new Dimension(226, 80));
        effectArea.setMinimumSize(new Dimension(226, 60));
        cell.add(effectArea);

        JLabel priceLabel = new JLabel(messages.getString("market.price") + ": " + item.price + " WC", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(priceLabel);

        JButton buyButton = new JButton(messages.getString("market.buy"));
        buyButton.setFont(new Font("Arial", Font.BOLD, 15));
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.addActionListener(e -> {
            if (statsManager != null && statsManager.getCoins() >= item.price) {
                boolean success = item.buyAction != null && item.buyAction.buy(this);
                if (success) {
                    statsManager.spendCoins(item.price);
                    updateCoinsLabel();
                    JOptionPane.showMessageDialog(this, messages.getString("market.buy.success"));
                    // После покупки обновляем карту
                    SwingUtilities.invokeLater(() -> {
                        Window win = SwingUtilities.getWindowAncestor(this);
                        if (win != null) win.repaint();
                    });
                } else {
                    JOptionPane.showMessageDialog(this, messages.getString("market.buy.fail"));
                }
            } else {
                JOptionPane.showMessageDialog(this, messages.getString("market.buy.notenough"));
            }
            updateCoinsLabel();
        });
        cell.add(Box.createVerticalStrut(8));
        cell.add(buyButton);

        return cell;
    }

    boolean buyDungBeetle(JComponent panel) {
        CompostSource compost = farmController.getCompostSource();
        DungBeetle beetle = new DungBeetle(compost.getX(), compost.getY(), compost, 2, 4, 50.0);
        farmController.addDungBeetle(beetle);

        for (PlantInstance plant : farmController.getPlantField().getPlants()) {
            if (plant.getAssignedBeetle() == null) {
                beetle.assignToPlant(plant);
                break;
            }
        }
        if (panel != null) panel.repaint();
        return true;
    }

    boolean buyPlant(JComponent panel) {
        if (farmController.getPlantField().isFull()) {
            JOptionPane.showMessageDialog(this, messages.getString("market.plant.fieldfull"));
            return false;
        }
        long now = System.currentTimeMillis();
        PlantInstance plant = farmController.getPlantField().tryAddPlant(now);

        if (plant != null) {
            for (DungBeetle beetle : farmController.getDungBeetles()) {
                if (beetle.getBeetleState() == DungBeetle.BeetleState.IDLE) {
                    beetle.assignToPlant(plant);
                    break;
                }
            }
        }
        if (panel != null) panel.repaint();
        return plant != null;
    }
}