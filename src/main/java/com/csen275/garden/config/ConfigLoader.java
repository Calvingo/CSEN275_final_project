package com.csen275.garden.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ConfigLoader {

    private static final String GARDEN_CONFIG_PATH = "config/garden_config.json";
    private static final String PLANT_DEFS_PATH = "config/plant_definitions.json";

    private ObjectMapper mapper;

    public ConfigLoader() {
        this.mapper = new ObjectMapper();
    }

    public GardenConfig loadGardenConfig() throws IOException {
        return mapper.readValue(Path.of(GARDEN_CONFIG_PATH).toFile(), GardenConfig.class);
    }

    public Map<String, PlantDefinitionConfig> loadPlantDefinitions() throws IOException {
        return mapper.readValue(
            Path.of(PLANT_DEFS_PATH).toFile(),
            mapper.getTypeFactory().constructMapType(Map.class, String.class, PlantDefinitionConfig.class)
        );
    }
}
