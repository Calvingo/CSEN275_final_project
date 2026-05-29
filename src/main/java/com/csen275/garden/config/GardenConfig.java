package com.csen275.garden.config;

import java.util.List;

public class GardenConfig {

    private List<PlantEntry> plants;

    public List<PlantEntry> getPlants() { return plants; }
    public void setPlants(List<PlantEntry> plants) { this.plants = plants; }

    public static class PlantEntry {
        private String name;
        private int amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }
}
