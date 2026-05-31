package com.csen275.garden;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GardenIntegrationTest {

    private LoggingService logger;
    private Garden garden;
    private SimulationEngine engine;

    @BeforeEach
    void setup() throws Exception {
        logger = new LoggingService();
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
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void fiveDaySimulationKeepsAtLeastOnePlantAlive() {
        engine.start();

        engine.onRain(10);
        engine.tickHour();

        engine.onTemperature(90);
        engine.tickHour();

        engine.onParasite("aphid");
        engine.tickHour();

        engine.onRain(15);
        engine.tickHour();

        engine.onTemperature(72);
        engine.tickHour();

        assertTrue(garden.getLivingCount() > 0);
    }

    @Test
    void logContainsInitAfterStart() throws Exception {
        engine.start();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasInit = false;

        for (String line : lines) {
            if (line.contains("INIT")) {
                hasInit = true;
            }
        }

        assertTrue(hasInit, "log.txt must contain INIT entry");
    }

    @Test
    void logContainsRainEntry() throws Exception {
        engine.start();
        engine.onRain(10);
        engine.tickHour();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasRain = false;

        for (String line : lines) {
            if (line.contains("RAIN")) {
                hasRain = true;
            }
        }

        assertTrue(hasRain, "log.txt must contain RAIN entry");
    }

    @Test
    void logContainsTemperatureEntry() throws Exception {
        engine.start();
        engine.onTemperature(90);
        engine.tickHour();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasTemp = false;

        for (String line : lines) {
            if (line.contains("TEMPERATURE")) {
                hasTemp = true;
            }
        }

        assertTrue(hasTemp, "log.txt must contain TEMPERATURE entry");
    }

    @Test
    void logContainsParasiteEntry() throws Exception {
        engine.start();
        engine.onParasite("aphid");
        engine.tickHour();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasParasite = false;

        for (String line : lines) {
            if (line.contains("PARASITE")) {
                hasParasite = true;
            }
        }

        assertTrue(hasParasite, "log.txt must contain PARASITE entry");
    }

    @Test
    void logLinesFollowCorrectFormat() throws Exception {
        engine.start();
        engine.onRain(10);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        for (String line : lines) {
            String[] parts = line.split(", ");
            assertTrue(parts.length >= 4, "Log line must have 4 fields: " + line);
        }
    }

    @Test
    void noUncaughtExceptionsDuringFiveDays() {
        assertDoesNotThrow(() -> {
            engine.start();

            engine.onRain(10);
            engine.tickHour();

            engine.onTemperature(90);
            engine.tickHour();

            engine.onParasite("aphid");
            engine.tickHour();

            engine.onRain(15);
            engine.tickHour();

            engine.onTemperature(72);
            engine.tickHour();
        });
    }
}
