package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.FertilizerSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FertilizerSystemTest {

    private Garden garden;
    private LoggingService logger;
    private FertilizerSystem fertilizer;

    @BeforeEach
    void setup() throws Exception {
        logger = new LoggingService();
        logger.clearLog();

        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        garden = new Garden();
        garden.loadFromConfig(config, defs);

        fertilizer = new FertilizerSystem(garden, logger);
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void fertilizeLowPlotsIncreasesNutrients() {
        Plot plot = garden.getGrid().getPlot(0, 0);
        plot.setNutrientLevel(10);

        fertilizer.fertilizeLowPlots(1);

        assertTrue(plot.getNutrientLevel() > 10);
    }

    @Test
    void manualFertilizerTreatsAllPlantedPlots() throws Exception {
        fertilizer.applyManualFertilizer(1);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasManual = false;
        for (String line : lines) {
            if (line.contains("MANUAL_FERTILIZER")) {
                hasManual = true;
            }
        }
        assertTrue(hasManual);
    }

    @Test
    void parasiteEventBoostsNutrients() throws Exception {
        Plot plot = garden.getGrid().getPlot(0, 0);
        int before = plot.getNutrientLevel();

        fertilizer.boostNutrientsAfterParasite(1);

        assertTrue(plot.getNutrientLevel() > before);
    }
}
