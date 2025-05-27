package com.wormfarm.farm.market;

public class MarketItem {
    public final String name;
    public final Type type;
    public final int price;
    public final String effect;
    public final BuyAction buyAction;

    public enum Type {
        INSECT, PLANT
    }

    @FunctionalInterface
    public interface BuyAction {
        boolean buy(javax.swing.JComponent panel);
    }

    public MarketItem(String name, Type type, int price, String effect, BuyAction buyAction) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.effect = effect;
        this.buyAction = buyAction;
    }
}