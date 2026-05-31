package com.csen275.garden.api;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.ClimateSystem;
import com.csen275.garden.module.FertilizerSystem;
import com.csen275.garden.module.GardenModule;
import com.csen275.garden.module.PestControlSystem;
import com.csen275.garden.module.WateringSystem;
import com.csen275.garden.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GardenSimulationAPI {

    private Garden garden;
    private LoggingService logger;
    private SimulationEngine engine;

    public GardenSimulationAPI() {
        this.logger = new LoggingService();
    }

    public void initializeGarden() {
        try {
            logger.clearLog();

            ConfigLoader loader = new ConfigLoader();
            GardenConfig config = loader.loadGardenConfig();
            Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

            garden = new Garden();
            garden.loadFromConfig(config, defs);

            List<GardenModule> modules = new ArrayList<GardenModule>();
            modules.add(new WateringSystem(garden, logger));
            modules.add(new ClimateSystem(garden, logger));
            modules.add(new PestControlSystem(garden, logger));
            modules.add(new FertilizerSystem(garden, logger));

            engine = new SimulationEngine(garden, logger, modules);
            engine.start();

        } catch (Exception e) {
            logger.log(0, "ERROR", "initializeGarden: " + e.getMessage(), 0);
        }
    }

    public Map<String, Object> getPlants() {
        List<String> names = new ArrayList<String>();
        List<Integer> waterRequirements = new ArrayList<Integer>();
        List<List<String>> parasites = new ArrayList<List<String>>();

        if (garden == null) {
            return emptyPlantMap();
        }

        for (PlantInstance plant : garden.getLivingPlants()) {
            names.add(plant.getType().getName());
            waterRequirements.add(plant.getType().getWaterRequirement());
            parasites.add(plant.getType().getParasites());
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("plants", names);
        result.put("waterRequirement", waterRequirements);
        result.put("parasites", parasites);

        return result;
    }

    public void rain(int amount) {
        if (!isReady()) {
            logger.log(0, "ERROR", "rain: garden not initialized", 0);
            return;
        }
        try {
            engine.onRain(amount);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(safeDay(), "ERROR", "rain: " + e.getMessage(), safeAliveCount());
        }
    }

    public void temperature(int fahrenheit) {
        if (!isReady()) {
            logger.log(0, "ERROR", "temperature: garden not initialized", 0);
            return;
        }
        try {
            engine.onTemperature(fahrenheit);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(safeDay(), "ERROR", "temperature: " + e.getMessage(), safeAliveCount());
        }
    }

    public void parasite(String name) {
        if (!isReady()) {
            logger.log(0, "ERROR", "parasite: garden not initialized", 0);
            return;
        }
        try {
            engine.onParasite(name);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(safeDay(), "ERROR", "parasite: " + e.getMessage(), safeAliveCount());
        }
    }

    public void getState() {
        if (!isReady()) {
            logger.log(0, "ERROR", "getState: garden not initialized", 0);
            return;
        }
        try {
            logger.logState(engine.getCurrentDay(), garden);
        } catch (Exception e) {
            logger.log(safeDay(), "ERROR", "getState: " + e.getMessage(), safeAliveCount());
        }
    }

    private boolean isReady() {
        return garden != null && engine != null;
    }

    private int safeDay() {
        return engine != null ? engine.getCurrentDay() : 0;
    }

    private int safeAliveCount() {
        return garden != null ? garden.getLivingCount() : 0;
    }

    private Map<String, Object> emptyPlantMap() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("plants", new ArrayList<String>());
        result.put("waterRequirement", new ArrayList<Integer>());
        result.put("parasites", new ArrayList<List<String>>());
        return result;
    }
}
