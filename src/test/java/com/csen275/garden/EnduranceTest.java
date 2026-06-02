package com.csen275.garden;

import com.csen275.garden.api.GardenSimulationAPI;
import com.csen275.garden.simulation.EnvironmentEventGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnduranceTest {

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Path.of("log.txt"));
    }

    @Test
    void twentyFourRandomEventsCompleteWithSurvivingPlantsAndStateLog() throws Exception {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();
        Random random = new Random(275);

        assertDoesNotThrow(() -> {
            for (int hour = 1; hour <= 24; hour++) {
                EnvironmentEventGenerator.applyRandomEvent(api, random);
            }
            api.getState();
        });

        int alive = ((List<?>) api.getPlants().get("plants")).size();
        assertTrue(alive > 0, "Garden should keep at least one plant alive after 24 random events");

        List<String> logLines = Files.readAllLines(Path.of("log.txt"));
        assertTrue(logLines.stream().anyMatch(line -> line.contains(", STATE, ")));
    }
}
