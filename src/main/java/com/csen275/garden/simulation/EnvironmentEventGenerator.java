package com.csen275.garden.simulation;

import com.csen275.garden.api.GardenSimulationAPI;

import java.util.Random;

/**
 * Shared random daily environment events used by the headless runner and JavaFX auto-simulation.
 * Matches the course API test pattern: each simulated day gets rain, temperature, or a parasite.
 */
public final class EnvironmentEventGenerator {

    private static final String[] PARASITE_TYPES = {
        "aphid", "spider_mite", "hornworm", "whitefly", "slug", "thrip", "bark_beetle"
    };

    private EnvironmentEventGenerator() {
    }

    public static void applyRandomEvent(SimulationEngine engine, Random random) {
        applyChoice(random.nextInt(3), random,
            amount -> engine.onRain(amount),
            temp -> engine.onTemperature(temp),
            name -> engine.onParasite(name));
    }

    public static void applyRandomEvent(GardenSimulationAPI api, Random random) {
        applyChoice(random.nextInt(3), random,
            api::rain,
            api::temperature,
            api::parasite);
    }

    private static void applyChoice(
        int choice,
        Random random,
        IntConsumer rain,
        IntConsumer temperature,
        StringConsumer parasite) {

        if (choice == 0) {
            rain.accept(5 + random.nextInt(20));
        } else if (choice == 1) {
            temperature.accept(40 + random.nextInt(81));
        } else {
            parasite.accept(PARASITE_TYPES[random.nextInt(PARASITE_TYPES.length)]);
        }
    }

    @FunctionalInterface
    private interface IntConsumer {
        void accept(int value);
    }

    @FunctionalInterface
    private interface StringConsumer {
        void accept(String value);
    }
}
