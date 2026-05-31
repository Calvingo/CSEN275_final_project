package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.WateringSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WateringSystemTest {

    private Garden garden;
    private LoggingService logger;
    private WateringSystem watering;

    @BeforeEach
    void setup() throws Exception {
        logger = new LoggingService();
        logger.clearLog();

        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        garden = new Garden();
        garden.loadFromConfig(config, defs);

        watering = new WateringSystem(garden, logger);
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void rainIncreasesSoilMoisture() {
        Plot plot = garden.getGrid().getPlot(0, 0);
        int before = plot.getSoilMoisture();

        watering.handleRain(1, 25);

        assertTrue(plot.getSoilMoisture() > before);
    }

    @Test
    void rainSetsRainedTodayFlag() {
        assertFalse(watering.isRainedToday());
        watering.handleRain(1, 10);
        assertTrue(watering.isRainedToday());
    }

    @Test
    void onDayEndClearsRainFlagForNextDay() {
        watering.handleRain(1, 10);
        assertTrue(watering.isRainedToday());
        watering.onDayEnd(1);
        assertFalse(watering.isRainedToday());
    }

    @Test
    void sprinklersActivateOnDryPlot() {
        Plot plot = garden.getGrid().getPlot(0, 0);
        plot.setSoilMoisture(5);

        watering.activateSprinklers(1);

        assertTrue(plot.getSoilMoisture() > 5);
    }

    @Test
    void resetDailyMoistureDropsHighMoisture() {
        Plot plot = garden.getGrid().getPlot(0, 0);
        plot.setSoilMoisture(80);

        watering.resetDailyMoisture();

        assertEquals(30, plot.getSoilMoisture());
    }

    @Test
    void onDayEndResetsIfNoRain() {
        Plot plot = garden.getGrid().getPlot(0, 0);
        plot.setSoilMoisture(80);

        watering.onDayEnd(1);

        assertEquals(30, plot.getSoilMoisture());
    }

    @Test
    void onDayEndDoesNotResetIfRainedToday() {
        watering.handleRain(1, 50);

        Plot plot = garden.getGrid().getPlot(0, 0);
        int afterRain = plot.getSoilMoisture();

        watering.onDayEnd(1);

        assertEquals(afterRain, plot.getSoilMoisture());
    }
}
