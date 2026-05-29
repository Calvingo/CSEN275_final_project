package com.csen275.garden.api;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.ClimateSystem;
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
        try {
            engine.onRain(amount);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(engine.getCurrentDay(), "ERROR", "rain: " + e.getMessage(), garden.getLivingCount());
        }
    }

    public void temperature(int fahrenheit) {
        try {
            engine.onTemperature(fahrenheit);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(engine.getCurrentDay(), "ERROR", "temperature: " + e.getMessage(), garden.getLivingCount());
        }
    }

    public void parasite(String name) {
        try {
            engine.onParasite(name);
            engine.tickHour();
        } catch (Exception e) {
            logger.log(engine.getCurrentDay(), "ERROR", "parasite: " + e.getMessage(), garden.getLivingCount());
        }
    }

    public void getState() {
        try {
            logger.logState(engine.getCurrentDay(), garden);
        } catch (Exception e) {
            logger.log(0, "ERROR", "getState: " + e.getMessage(), 0);
        }
    }
}
