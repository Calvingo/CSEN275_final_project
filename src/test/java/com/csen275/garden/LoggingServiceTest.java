package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.logging.LoggingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoggingServiceTest {

    private LoggingService logger;

    @BeforeEach
    void setup() {
        logger = new LoggingService();
        logger.clearLog();
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void logLineMatchesExpectedFormat() throws Exception {
        logger.log(0, "INIT", "config_loaded", 10);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        assertEquals(1, lines.size());
        assertEquals("0, INIT, config_loaded, 10", lines.get(0));
    }

    @Test
    void logAppendsWithoutOverwrite() throws Exception {
        logger.log(1, "RAIN", "25", 10);
        logger.log(2, "TEMPERATURE", "105", 9);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        assertEquals(2, lines.size());
        assertEquals("1, RAIN, 25, 10", lines.get(0));
        assertEquals("2, TEMPERATURE, 105, 9", lines.get(1));
    }

    @Test
    void logStateWritesStateLine() throws Exception {
        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        Garden garden = new Garden();
        garden.loadFromConfig(config, defs);

        logger.logState(1, garden);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("STATE"));
        assertTrue(lines.get(0).contains("alive="));
    }

    @Test
    void clearLogRemovesFile() throws Exception {
        logger.log(0, "TEST", "value", 5);
        logger.clearLog();

        assertFalse(Files.exists(Path.of("log.txt")));
    }

    @Test
    void multipleLogsHaveCorrectDayField() throws Exception {
        logger.log(3, "PARASITE", "aphid", 8);

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        assertTrue(lines.get(0).startsWith("3,"));
    }
}
