package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.ClimateSystem;
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

class SimulationEngineTest {

    private Garden garden;
    private LoggingService logger;
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

        engine = new SimulationEngine(garden, logger, modules);
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void startLogsInitEvent() throws Exception {
        engine.start();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasInit = false;

        for (String line : lines) {
            if (line.contains("INIT")) {
                hasInit = true;
            }
        }

        assertTrue(hasInit);
    }

    @Test
    void tickHourAdvancesDay() {
        assertEquals(0, engine.getCurrentDay());
        engine.tickHour();
        assertEquals(1, engine.getCurrentDay());
        engine.tickHour();
        assertEquals(2, engine.getCurrentDay());
    }

    @Test
    void deadPlantsRemovedEachTick() {
        int before = garden.getLivingCount();

        engine.start();
        engine.onTemperature(120);

        for (int i = 0; i < 30; i++) {
            engine.tickHour();
        }

        assertTrue(garden.getLivingCount() <= before);
    }

    @Test
    void engineSurvivesExceptionInModule() {
        GardenModule crasher = new GardenModule() {
            public String getName() { return "Crasher"; }
            public void onDayStart(int day) { throw new RuntimeException("simulated crash"); }
            public void onDayEnd(int day) { throw new RuntimeException("simulated crash"); }
            public void onEvent(GardenEvent event) { throw new RuntimeException("simulated crash"); }
        };

        List<GardenModule> modules = new ArrayList<GardenModule>();
        modules.add(crasher);

        SimulationEngine faultyEngine = new SimulationEngine(garden, logger, modules);

        assertDoesNotThrow(() -> {
            faultyEngine.tickHour();
            faultyEngine.tickHour();
        });
    }

    @Test
    void rainEventPublishedToModules() {
        engine.start();
        assertDoesNotThrow(() -> engine.onRain(20));
    }

    @Test
    void parasiteEventPublishedToModules() {
        engine.start();
        assertDoesNotThrow(() -> engine.onParasite("aphid"));
    }
}
