package com.csen275.garden;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ScaffoldTest {

    @Test
    void configFilesExist() {
        assertTrue(Files.exists(Path.of("config/garden_config.json")),
                "config/garden_config.json must exist");
        assertTrue(Files.exists(Path.of("config/plant_definitions.json")),
                "config/plant_definitions.json must exist");
    }

    @Test
    void packageDirectoriesExist() {
        assertTrue(Files.isDirectory(Path.of("src/main/java/com/csen275/garden/api")));
        assertTrue(Files.isDirectory(Path.of("src/main/java/com/csen275/garden/domain/plant")));
        assertTrue(Files.isDirectory(Path.of("src/main/java/com/csen275/garden/module")));
        assertTrue(Files.isDirectory(Path.of("src/main/java/com/csen275/garden/simulation")));
        assertTrue(Files.isDirectory(Path.of("src/main/java/com/csen275/garden/logging")));
    }
}
