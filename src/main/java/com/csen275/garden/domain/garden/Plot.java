package com.csen275.garden.domain.garden;

import com.csen275.garden.domain.plant.PlantInstance;

public class Plot {

    private int soilMoisture;
    private PlantInstance plant;

    public Plot() {
        this.soilMoisture = 20;
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

    public void tickDay() {
        soilMoisture = soilMoisture - 10;
        if (soilMoisture < 0) {
            soilMoisture = 0;
        }
        if (plant != null && plant.isAlive()) {
            plant.tickDaily();
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
    public void setSoilMoisture(int value) { this.soilMoisture = value; }
}
