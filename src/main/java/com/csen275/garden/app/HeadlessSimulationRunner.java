package com.csen275.garden.app;

import com.csen275.garden.api.GardenSimulationAPI;
import com.csen275.garden.simulation.EnvironmentEventGenerator;

import java.util.Random;

public class HeadlessSimulationRunner {

    private static final long REAL_HOUR_MS = 3_600_000L;

    public static void main(String[] args) throws InterruptedException {
        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();

        Random random = new Random();

        long startedAt = System.currentTimeMillis();
        boolean keepAlive = Boolean.parseBoolean(System.getProperty("garden.keepAlive", "false"));
        long keepAliveHours = Long.parseLong(System.getProperty("garden.keepAliveHours", "24"));

        for (int hour = 1; hour <= 24; hour++) {
            EnvironmentEventGenerator.applyRandomEvent(api, random);
        }

        api.getState();
        System.out.println("24 simulated days complete. Final plant count: "
            + ((java.util.List<?>) api.getPlants().get("plants")).size());
        System.out.println("See log.txt for the full event history.");

        if (keepAlive) {
            System.out.println("Endurance mode: keeping JVM alive for " + keepAliveHours + " real hour(s).");
            while (System.currentTimeMillis() - startedAt < keepAliveHours * REAL_HOUR_MS) {
                Thread.sleep(REAL_HOUR_MS);
                api.getState();
                System.out.println("Hourly state logged. Plants alive: "
                    + ((java.util.List<?>) api.getPlants().get("plants")).size());
            }
            System.out.println("Endurance window complete.");
        }
    }
}
