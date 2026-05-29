package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.ClimateSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClimateSystemTest {

    private Garden garden;
    private LoggingService logger;
    private ClimateSystem climate;

    @BeforeEach
    void setup() throws Exception {
        logger = new LoggingService();
        logger.clearLog();

        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        garden = new Garden();
        garden.loadFromConfig(config, defs);

        climate = new ClimateSystem(garden, logger);
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void validTemperatureIsAccepted() {
        climate.setTemperature(1, 105);
        assertEquals(105, climate.getCurrentTempF());
    }

    @Test
    void invalidTemperatureIsRejectedAndDoesNotCrash() {
        int before = climate.getCurrentTempF();
        climate.setTemperature(1, 999);
        assertEquals(before, climate.getCurrentTempF());
    }

    @Test
    void lowBoundaryTemperatureAccepted() {
        climate.setTemperature(1, 40);
        assertEquals(40, climate.getCurrentTempF());
    }

    @Test
    void highBoundaryTemperatureAccepted() {
        climate.setTemperature(1, 120);
        assertEquals(120, climate.getCurrentTempF());
    }

    @Test
    void hotTemperatureReducesPlantHealth() {
        PlantInstance first = garden.getLivingPlants().get(0);
        int healthBefore = first.getHealth();

        climate.setTemperature(1, 105);
        climate.applyThermalStress(1);

        assertTrue(first.getHealth() < healthBefore);
    }

    @Test
    void normalTemperatureDoesNotStressPlants() {
        PlantInstance first = garden.getLivingPlants().get(0);
        int healthBefore = first.getHealth();

        climate.setTemperature(1, 72);
        climate.applyThermalStress(1);

        assertEquals(healthBefore, first.getHealth());
    }

    @Test
    void stressIsGradualNotInstantDeath() {
        climate.setTemperature(1, 105);
        climate.applyThermalStress(1);

        for (PlantInstance plant : garden.getLivingPlants()) {
            assertTrue(plant.getHealth() > 0);
        }
    }

    @Test
    void endOfDayResetsTemperatureToDefault() {
        climate.setTemperature(1, 105);
        climate.onDayEnd(1);
        assertEquals(72, climate.getCurrentTempF());
    }
}
