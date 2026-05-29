package com.csen275.garden.domain.plant;

import java.util.List;
import java.util.UUID;

public class PlantType {

    private String name;
    private int waterRequirement;
    private int healRate;
    private List<String> parasites;

    public PlantType(String name, int waterRequirement, int healRate, List<String> parasites) {
        this.name = name;
        this.waterRequirement = waterRequirement;
        this.healRate = healRate;
        this.parasites = parasites;
    }

    public PlantInstance createInstance() {
        return new PlantInstance(UUID.randomUUID().toString(), this);
    }

    public String getName() { return name; }
    public int getWaterRequirement() { return waterRequirement; }
    public int getHealRate() { return healRate; }
    public List<String> getParasites() { return parasites; }
}
