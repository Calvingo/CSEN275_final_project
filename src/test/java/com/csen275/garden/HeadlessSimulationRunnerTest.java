package com.csen275.garden;

import com.csen275.garden.api.GardenSimulationAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HeadlessSimulationRunnerTest {

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Path.of("log.txt"));
    }

    @Test
    void twentyFourHourRunCompletesWithoutException() {
        assertDoesNotThrow(() -> {
            GardenSimulationAPI api = new GardenSimulationAPI();
            api.initializeGarden();

            String[] parasiteTypes = {"aphid", "spider_mite", "hornworm", "whitefly", "slug"};
            Random random = new Random(42);

            for (int hour = 1; hour <= 24; hour++) {
                int choice = random.nextInt(3);
                if (choice == 0) {
                    api.rain(5 + random.nextInt(20));
                } else if (choice == 1) {
                    api.temperature(40 + random.nextInt(81));
                } else {
                    api.parasite(parasiteTypes[random.nextInt(parasiteTypes.length)]);
                }
            }

            api.getState();
        });
    }

    @Test
    void logHasEntriesAfterRun() throws Exception {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();

        for (int i = 0; i < 24; i++) {
            api.rain(10);
        }

        api.getState();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        assertFalse(lines.isEmpty());
    }

    @Test
    void plantsAliveCountInLogIsNonNegative() throws Exception {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();

        for (int i = 0; i < 24; i++) {
            api.rain(15);
        }

        api.getState();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));

        for (String line : lines) {
            String[] parts = line.split(", ");
            if (parts.length >= 4) {
                int plantsAlive = Integer.parseInt(parts[3].trim());
                assertTrue(plantsAlive >= 0);
            }
        }
    }
}
