package com.csen275.garden.ui;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.ClimateSystem;
import com.csen275.garden.module.FertilizerSystem;
import com.csen275.garden.module.GardenModule;
import com.csen275.garden.module.PestControlSystem;
import com.csen275.garden.module.WateringSystem;
import com.csen275.garden.simulation.SimulationEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UI-side wiring that mirrors {@link com.csen275.garden.api.GardenSimulationAPI}
 * initialization without modifying the API or domain layers.
 */
public class GardenUiSession {

    private Garden garden;
    private LoggingService logger;
    private SimulationEngine engine;
    private WateringSystem wateringSystem;
    private ClimateSystem climateSystem;
    private PestControlSystem pestControlSystem;
    private FertilizerSystem fertilizerSystem;

    public void initialize() throws IOException {
        logger = new LoggingService();
        logger.clearLog();

        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> definitions = loader.loadPlantDefinitions();

        garden = new Garden();
        garden.loadFromConfig(config, definitions);

        wateringSystem = new WateringSystem(garden, logger);
        climateSystem = new ClimateSystem(garden, logger);
        pestControlSystem = new PestControlSystem(garden, logger);
        fertilizerSystem = new FertilizerSystem(garden, logger);

        List<GardenModule> modules = new ArrayList<GardenModule>();
        modules.add(wateringSystem);
        modules.add(climateSystem);
        modules.add(pestControlSystem);
        modules.add(fertilizerSystem);

        engine = new SimulationEngine(garden, logger, modules);
        engine.start();
    }

    public boolean isInitialized() {
        return garden != null && engine != null;
    }

    public Garden getGarden() {
        return garden;
    }

    public LoggingService getLogger() {
        return logger;
    }

    public SimulationEngine getEngine() {
        return engine;
    }

    public WateringSystem getWateringSystem() {
        return wateringSystem;
    }

    public ClimateSystem getClimateSystem() {
        return climateSystem;
    }

    public PestControlSystem getPestControlSystem() {
        return pestControlSystem;
    }

    public FertilizerSystem getFertilizerSystem() {
        return fertilizerSystem;
    }
}
