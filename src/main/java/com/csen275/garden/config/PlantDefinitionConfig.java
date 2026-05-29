package com.csen275.garden.config;

import java.util.List;

public class PlantDefinitionConfig {

    private int waterRequirement;
    private int healRate;
    private List<String> parasites;

    public int getWaterRequirement() { return waterRequirement; }
    public void setWaterRequirement(int waterRequirement) { this.waterRequirement = waterRequirement; }
    public int getHealRate() { return healRate; }
    public void setHealRate(int healRate) { this.healRate = healRate; }
    public List<String> getParasites() { return parasites; }
    public void setParasites(List<String> parasites) { this.parasites = parasites; }
}
