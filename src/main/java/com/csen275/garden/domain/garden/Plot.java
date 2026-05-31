package com.csen275.garden.domain.garden;

import com.csen275.garden.domain.plant.PlantInstance;

public class Plot {

    private static final int DEFAULT_NUTRIENT_LEVEL = 50;

    private int soilMoisture;
    private int nutrientLevel;
    private PlantInstance plant;

    public Plot() {
        this.soilMoisture = 20;
        this.nutrientLevel = DEFAULT_NUTRIENT_LEVEL;
        this.plant = null;
    }

    public void applyWater(int amount) {
        soilMoisture = soilMoisture + amount;
        if (soilMoisture > 100) {
            soilMoisture = 100;
        }
        if (plant != null && plant.isAlive()) {
            plant.applyWater(amount);
        }
    }

    public void applyFertilizer(int amount) {
        nutrientLevel = nutrientLevel + amount;
        if (nutrientLevel > 100) {
            nutrientLevel = 100;
        }
    }

    public void tickDay() {
        soilMoisture = soilMoisture - 10;
        if (soilMoisture < 0) {
            soilMoisture = 0;
        }
        nutrientLevel = nutrientLevel - 3;
        if (nutrientLevel < 0) {
            nutrientLevel = 0;
        }
        if (plant != null && plant.isAlive()) {
            plant.tickDaily(nutrientLevel);
        }
    }

    public void setPlant(PlantInstance plant) {
        this.plant = plant;
    }

    public void clearPlant() {
        this.plant = null;
    }

    public PlantInstance getPlant() { return plant; }
    public int getSoilMoisture() { return soilMoisture; }
    public int getNutrientLevel() { return nutrientLevel; }
    public void setSoilMoisture(int value) { this.soilMoisture = value; }
    public void setNutrientLevel(int value) { this.nutrientLevel = value; }
}
