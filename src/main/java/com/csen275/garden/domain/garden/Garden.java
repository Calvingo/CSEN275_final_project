package com.csen275.garden.domain.garden;

import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.domain.plant.PlantType;

import java.util.ArrayList;
import java.util.List;

public class Garden {

    private GardenGrid grid;
    private List<PlantInstance> livingPlants;
    private int nextRow;
    private int nextCol;

    public Garden() {
        this.grid = new GardenGrid(5, 5);
        this.livingPlants = new ArrayList<PlantInstance>();
        this.nextRow = 0;
        this.nextCol = 0;
    }

    public boolean placePlantOnGrid(PlantInstance plant) {
        // Find the next open slot
        while (nextRow < grid.getRows()) {
            if (grid.placePlant(plant, nextRow, nextCol)) {
                livingPlants.add(plant);
                advance();
                return true;
            }
            advance();
        }
        return false;
    }

    private void advance() {
        nextCol++;
        if (nextCol >= grid.getCols()) {
            nextCol = 0;
            nextRow++;
        }
    }

    public void removeDead() {
        List<PlantInstance> toRemove = new ArrayList<PlantInstance>();
        for (PlantInstance p : livingPlants) {
            if (!p.isAlive()) {
                toRemove.add(p);
            }
        }
        for (PlantInstance p : toRemove) {
            livingPlants.remove(p);
            clearFromGrid(p);
        }
    }

    private void clearFromGrid(PlantInstance plant) {
        for (Plot plot : grid.getAllPlots()) {
            if (plot.getPlant() == plant) {
                plot.clearPlant();
                return;
            }
        }
    }

    public int getLivingCount() {
        return livingPlants.size();
    }

    public List<PlantInstance> getLivingPlants() {
        return livingPlants;
    }

    public GardenGrid getGrid() {
        return grid;
    }
}
