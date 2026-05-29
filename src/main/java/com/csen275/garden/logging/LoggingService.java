package com.csen275.garden.logging;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoggingService {

    private static final String LOG_PATH = "log.txt";

    public synchronized void log(int day, String event, String eventValue, int plantsAlive) {

        String line = day + ", " + event + ", " + eventValue + ", " + plantsAlive;

        try {
            FileWriter fileWriter = new FileWriter(LOG_PATH, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(line);
            printWriter.close();
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public synchronized void logState(Garden garden) {

        int alive = garden.getLivingCount();
        StringBuilder details = new StringBuilder();

        for (PlantInstance plant : garden.getLivingPlants()) {
            details.append(plant.getType().getName());
            details.append("(health=");
            details.append(plant.getHealth());
            details.append(")");
            details.append(" ");
        }

        String eventValue = "alive=" + alive + " plants=[" + details.toString().trim() + "]";

        log(0, "STATE", eventValue, alive);
    }

    public synchronized void logState(int day, Garden garden) {

        int alive = garden.getLivingCount();
        StringBuilder details = new StringBuilder();

        for (PlantInstance plant : garden.getLivingPlants()) {
            details.append(plant.getType().getName());
            details.append("(health=");
            details.append(plant.getHealth());
            details.append(")");
            details.append(" ");
        }

        String eventValue = "alive=" + alive + " plants=[" + details.toString().trim() + "]";

        log(day, "STATE", eventValue, alive);
    }

    public void clearLog() {

        try {
            Files.deleteIfExists(Path.of(LOG_PATH));
        } catch (IOException e) {
            System.err.println("Failed to clear log: " + e.getMessage());
        }
    }
}
