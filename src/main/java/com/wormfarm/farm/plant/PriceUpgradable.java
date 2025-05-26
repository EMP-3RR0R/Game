package com.wormfarm.farm.plant;

public interface PriceUpgradable {
    void setPriceMultiplier(double mul);
    void setGrowthSpeedMultiplier(double mul);
    double getPriceMultiplier();
    double getGrowthSpeedMultiplier();
}