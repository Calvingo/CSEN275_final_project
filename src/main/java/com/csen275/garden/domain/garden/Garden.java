package com.csen275.garden.domain.garden;

import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.domain.plant.PlantType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<String> removeDead() {
        List<PlantInstance> toRemove = new ArrayList<PlantInstance>();
        List<String> deaths = new ArrayList<String>();

        for (PlantInstance p : livingPlants) {
            if (!p.isAlive()) {
                toRemove.add(p);
                deaths.add(formatPlantLocation(p));
            }
        }

        for (PlantInstance p : toRemove) {
            livingPlants.remove(p);
            clearFromGrid(p);
        }

        return deaths;
    }

    private String formatPlantLocation(PlantInstance plant) {
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                Plot plot = grid.getPlot(row, col);
                if (plot.getPlant() == plant) {
                    return plant.getType().getName() + "@plot(" + row + "," + col + ")";
                }
            }
        }
        return plant.getType().getName() + "@plot(unknown)";
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

    public void loadFromConfig(GardenConfig gardenConfig, Map<String, PlantDefinitionConfig> definitions) {
        for (GardenConfig.PlantEntry entry : gardenConfig.getPlants()) {
            PlantDefinitionConfig def = definitions.get(entry.getName());
            if (def == null) {
                continue;
            }
            PlantType type = new PlantType(
                entry.getName(),
                def.getWaterRequirement(),
                def.getHealRate(),
                def.getParasites()
            );
            for (int i = 0; i < entry.getAmount(); i++) {
                PlantInstance instance = type.createInstance();
                placePlantOnGrid(instance);
            }
        }
    }

    public List<String> tickDay() {
        for (Plot plot : grid.getAllPlots()) {
            plot.tickDay();
        }
        return removeDead();
    }

    public List<String> removeDeadAndLog(com.csen275.garden.logging.LoggingService logger, int day) {
        List<String> deaths = removeDead();
        for (String death : deaths) {
            logger.log(day, "PLANT_DEATH", death, getLivingCount());
        }
        return deaths;
    }

    public GardenGrid getGrid() {
        return grid;
    }
}
