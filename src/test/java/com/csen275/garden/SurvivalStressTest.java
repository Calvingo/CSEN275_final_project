package com.csen275.garden;

import com.csen275.garden.api.GardenSimulationAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SurvivalStressTest {

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Path.of("log.txt"));
    }

    @Test
    void allExtremeHeatStillLeavesSurvivors() {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();
        for (int i = 0; i < 24; i++) {
            api.temperature(120);
        }
        int alive = plantCount(api);
        System.out.println("all heat 120 alive=" + alive);
        assertTrue(alive > 0, "Garden should not wipe out on extreme heat alone");
    }

    @Test
    void noRainMildDaysStillLeaveSurvivors() {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();
        for (int i = 0; i < 24; i++) {
            api.temperature(72);
        }
        int alive = plantCount(api);
        System.out.println("no rain mild alive=" + alive);
        assertTrue(alive > 0, "Garden should survive mild days with automated watering");
    }

    @Test
    void randomSeedsRarelyWipeOut() {
        String[] parasites = {"aphid", "spider_mite", "hornworm", "whitefly", "slug", "thrip", "bark_beetle"};
        int wipeouts = 0;
        for (int seed = 0; seed < 100; seed++) {
            GardenSimulationAPI api = new GardenSimulationAPI();
            api.initializeGarden();
            Random r = new Random(seed);
            for (int h = 1; h <= 24; h++) {
                int c = r.nextInt(3);
                if (c == 0) {
                    api.rain(5 + r.nextInt(20));
                } else if (c == 1) {
                    api.temperature(40 + r.nextInt(81));
                } else {
                    api.parasite(parasites[r.nextInt(parasites.length)]);
                }
            }
            if (plantCount(api) == 0) {
                wipeouts++;
            }
        }
        System.out.println("wipeouts in 100 random runs=" + wipeouts);
        assertTrue(wipeouts < 20, "Too many total wipeouts: " + wipeouts);
    }

    private int plantCount(GardenSimulationAPI api) {
        Map<String, Object> plants = api.getPlants();
        return ((List<?>) plants.get("plants")).size();
    }
}
