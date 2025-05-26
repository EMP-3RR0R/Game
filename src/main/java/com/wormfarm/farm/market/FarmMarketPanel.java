package com.wormfarm.farm.market;

import com.wormfarm.gui.base.BaseInternalFrame;
import com.wormfarm.farm.FarmController;
import com.wormfarm.farm.resource.CompostSource;
import com.wormfarm.farm.resource.Beehive;
import com.wormfarm.farm.resource.Anthill;
import com.wormfarm.farm.insect.DungBeetle;
import com.wormfarm.farm.insect.Bee;
import com.wormfarm.farm.insect.Ant;
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
        // Фиксированный размер, чтобы вместить все элементы
        setSize(740, 450);
        setPreferredSize(new Dimension(740, 450));
        setResizable(false); // Запрещаем изменение размера
        setLayout(new BorderLayout(0, 5));
        setClosable(true);

        setOpaque(false);
        setBackground(new Color(255, 255, 255, 220));

        // Верхняя панель с балансом
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        topPanel.setOpaque(false);
        coinsLabel = new JLabel();
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        updateCoinsLabel();
        topPanel.add(coinsLabel);
        add(topPanel, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        // Используем GridBagLayout для точного позиционирования
        itemsPanel.setLayout(new GridBagLayout());
        add(itemsPanel, BorderLayout.CENTER); // Убираем JScrollPane полностью

        loadMarketItems();
        updateComponents();

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        loadMarketItems();
        updateComponents();

        if (gameSessionManager != null && gameSessionManager.getCurrentMapPanel() != null) {
            gameSessionManager.getCurrentMapPanel().pauseGame();
        }

        addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                if (gameSessionManager != null) {
                    gameSessionManager.onResumableWindowClosed();
                    WormMapPanel mapPanel = gameSessionManager.getCurrentMapPanel();
                    if (mapPanel != null) mapPanel.repaint();
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

    protected void updateComponents() {
        itemsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Отступы между элементами
        gbc.fill = GridBagConstraints.BOTH;

        // Первый ряд
        gbc.gridy = 0;
        gbc.gridx = 0;
        itemsPanel.add(createMarketCell(marketItems.get(0)), gbc);

        gbc.gridx = 1;
        itemsPanel.add(createMarketCell(marketItems.get(1)), gbc);

        // Второй ряд
        gbc.gridy = 1;
        gbc.gridx = 0;
        itemsPanel.add(createMarketCell(marketItems.get(2)), gbc);

        gbc.gridx = 1;
        itemsPanel.add(createMarketCell(marketItems.get(3)), gbc);

        itemsPanel.revalidate();
        itemsPanel.repaint();
        updateCoinsLabel();
    }

    private void loadMarketItems() {
        marketItems.clear();
        // Навозник
        marketItems.add(new MarketItem(
                messages.getString("market.dungbeetle.name"),
                MarketItem.Type.INSECT,
                30,
                messages.getString("market.dungbeetle.effect"),
                (panel) -> buyDungBeetle(panel)
        ));
        // Растение
        marketItems.add(new MarketItem(
                messages.getString("market.plant.name"),
                MarketItem.Type.PLANT,
                10,
                messages.getString("market.plant.effect"),
                (panel) -> buyPlant(panel)
        ));
        // Пчела
        marketItems.add(new MarketItem(
                messages.getString("market.bee.name"),
                MarketItem.Type.INSECT,
                50,
                messages.getString("market.bee.effect"),
                (panel) -> buyBee(panel)
        ));
        // Муравей
        marketItems.add(new MarketItem(
                messages.getString("market.ant.name"),
                MarketItem.Type.INSECT,
                50,
                messages.getString("market.ant.effect"),
                (panel) -> buyAnt(panel)
        ));
    }

    private JPanel createMarketCell(MarketItem item) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        // Уменьшаем размеры ячеек
        cell.setPreferredSize(new Dimension(320, 170));
        cell.setMaximumSize(new Dimension(320, 170));
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Уменьшаем шрифт
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(nameLabel);

        JLabel typeLabel = new JLabel(messages.getString("market.type." + item.type.name().toLowerCase()), SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        typeLabel.setForeground(Color.DARK_GRAY);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(typeLabel);

        JTextArea effectArea = new JTextArea(item.effect);
        effectArea.setOpaque(false);
        effectArea.setFont(new Font("Arial", Font.PLAIN, 12)); // Уменьшаем шрифт
        effectArea.setLineWrap(true);
        effectArea.setWrapStyleWord(true);
        effectArea.setEditable(false);
        effectArea.setFocusable(false);
        effectArea.setBackground(new Color(0,0,0,0));
        effectArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        effectArea.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        effectArea.setMaximumSize(new Dimension(250, 60)); // Уменьшаем высоту
        cell.add(effectArea);

        JLabel priceLabel = new JLabel(messages.getString("market.price") + ": " + item.price + " WC", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(priceLabel);

        JButton buyButton = new JButton(messages.getString("market.buy"));
        buyButton.setFont(new Font("Arial", Font.BOLD, 14));
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.setMaximumSize(new Dimension(150, 30));
        buyButton.addActionListener(e -> {
            if (statsManager != null && statsManager.getCoins() >= item.price) {
                boolean success = item.buyAction != null && item.buyAction.buy(this);
                if (success) {
                    statsManager.spendCoins(item.price);
                    updateCoinsLabel();
                    JOptionPane.showMessageDialog(this, messages.getString("market.buy.success"));
                } else {
                    JOptionPane.showMessageDialog(this, messages.getString("market.buy.fail"));
                }
            } else {
                JOptionPane.showMessageDialog(this, messages.getString("market.buy.notenough"));
            }
            updateCoinsLabel();
        });
        cell.add(Box.createVerticalStrut(5));
        cell.add(buyButton);

        return cell;
    }

    boolean buyDungBeetle(JComponent panel) {
        CompostSource compost = farmController.getCompostSource();
        DungBeetle beetle = new DungBeetle(compost.getX(), compost.getY(), compost, 1, 3, 2.0);
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

    boolean buyBee(JComponent panel) {
        Beehive beehive = farmController.getBeehive();
        Bee bee = new Bee(beehive.getX(), beehive.getY(), beehive, 3);
        // Назначаем на первое растение без пчелы
        for (PlantInstance plant : farmController.getPlantField().getPlants()) {
            if (plant.getPriceMultiplier() == 1.0) {
                bee.assignToPlant(plant);
                break;
            }
        }
        farmController.addBee(bee);
        if (panel != null) panel.repaint();
        return true;
    }

    boolean buyAnt(JComponent panel) {
        Anthill anthill = farmController.getAnthill();
        Ant ant = new Ant(anthill.getX(), anthill.getY(), anthill, 2);
        // Назначаем на первое растение без муравья (по простоте: priceMultiplier == 1.0)
        for (PlantInstance plant : farmController.getPlantField().getPlants()) {
            if (plant.getGrowthSpeedMultiplier() == 1.0) {
                ant.assignToPlant(plant);
                break;
            }
        }
        farmController.addAnt(ant);
        if (panel != null) panel.repaint();
        return true;
    }
}