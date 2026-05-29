package com.csen275.garden;

import com.csen275.garden.config.ConfigLoader;
import com.csen275.garden.config.GardenConfig;
import com.csen275.garden.config.PlantDefinitionConfig;
import com.csen275.garden.domain.garden.Garden;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void gardenConfigLoads() throws Exception {
        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        assertNotNull(config);
        assertFalse(config.getPlants().isEmpty());
    }

    @Test
    void plantDefinitionsLoad() throws Exception {
        ConfigLoader loader = new ConfigLoader();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();
        assertNotNull(defs);
        assertFalse(defs.isEmpty());
    }

    @Test
    void loadFromConfigMeetsMinimumCount() throws Exception {
        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        Garden garden = new Garden();
        garden.loadFromConfig(config, defs);

        assertTrue(garden.getLivingCount() >= 10, "Must have at least 10 living plants");
    }

    @Test
    void loadFromConfigHasAllVarieties() throws Exception {
        ConfigLoader loader = new ConfigLoader();
        GardenConfig config = loader.loadGardenConfig();
        Map<String, PlantDefinitionConfig> defs = loader.loadPlantDefinitions();

        Garden garden = new Garden();
        garden.loadFromConfig(config, defs);

        Set<String> types = new HashSet<>();
        garden.getLivingPlants().forEach(p -> types.add(p.getType().getName()));

        for (GardenConfig.PlantEntry entry : config.getPlants()) {
            assertTrue(types.contains(entry.getName()), "Missing plant type: " + entry.getName());
        }
    }
}
