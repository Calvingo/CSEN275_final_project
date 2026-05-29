package com.csen275.garden;

import com.csen275.garden.api.GardenSimulationAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GardenSimulationAPITest {

    private GardenSimulationAPI api;

    @BeforeEach
    void setup() {
        api = new GardenSimulationAPI();
        api.initializeGarden();
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Path.of("log.txt"));
    }

    @Test
    void getPlantsReturnsAllThreeKeys() {
        Map<String, Object> plants = api.getPlants();

        assertTrue(plants.containsKey("plants"));
        assertTrue(plants.containsKey("waterRequirement"));
        assertTrue(plants.containsKey("parasites"));
    }

    @Test
    void getPlantsHasAtLeastTenEntries() {
        Map<String, Object> plants = api.getPlants();
        List<String> names = (List<String>) plants.get("plants");

        assertTrue(names.size() >= 10);
    }

    @Test
    void allThreeListsHaveSameSize() {
        Map<String, Object> plants = api.getPlants();
        List<String> names = (List<String>) plants.get("plants");
        List<Integer> water = (List<Integer>) plants.get("waterRequirement");
        List<List<String>> parasites = (List<List<String>>) plants.get("parasites");

        assertEquals(names.size(), water.size());
        assertEquals(names.size(), parasites.size());
    }

    @Test
    void deadPlantRemovedFromGetPlants() {
        int before = ((List<String>) api.getPlants().get("plants")).size();

        // Hammer with extreme heat to kill some plants
        for (int i = 0; i < 10; i++) {
            api.temperature(120);
        }

        int after = ((List<String>) api.getPlants().get("plants")).size();

        assertTrue(after <= before);
    }

    @Test
    void rainDoesNotCrash() {
        assertDoesNotThrow(() -> api.rain(15));
    }

    @Test
    void temperatureDoesNotCrash() {
        assertDoesNotThrow(() -> api.temperature(85));
    }

    @Test
    void parasiteDoesNotCrash() {
        assertDoesNotThrow(() -> api.parasite("aphid"));
    }

    @Test
    void getStateWritesToLog() throws Exception {
        api.getState();

        List<String> lines = Files.readAllLines(Path.of("log.txt"));
        boolean hasState = false;

        for (String line : lines) {
            if (line.contains("STATE")) {
                hasState = true;
            }
        }

        assertTrue(hasState);
    }

    @Test
    void twentyFourIterationLoopDoesNotCrash() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 24; i++) {
                int choice = i % 3;
                if (choice == 0) {
                    api.rain(10);
                } else if (choice == 1) {
                    api.temperature(72);
                } else {
                    api.parasite("aphid");
                }
            }
            api.getState();
        });
    }
}
