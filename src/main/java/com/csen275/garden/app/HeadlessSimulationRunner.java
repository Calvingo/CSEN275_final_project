package com.csen275.garden.app;

import com.csen275.garden.api.GardenSimulationAPI;

import java.util.Random;

public class HeadlessSimulationRunner {

    public static void main(String[] args) {

        GardenSimulationAPI api = new GardenSimulationAPI();
        api.initializeGarden();

        Random random = new Random();

        String[] parasiteTypes = {"aphid", "spider_mite", "hornworm", "whitefly", "slug", "thrip", "bark_beetle"};

        for (int hour = 1; hour <= 24; hour++) {
            int choice = random.nextInt(3);

            if (choice == 0) {
                int rainAmount = 5 + random.nextInt(20);
                api.rain(rainAmount);

            } else if (choice == 1) {
                int temp = 40 + random.nextInt(81);
                api.temperature(temp);

            } else {
                String parasite = parasiteTypes[random.nextInt(parasiteTypes.length)];
                api.parasite(parasite);
            }
        }

        api.getState();

        System.out.println("24-hour simulation complete. Check log.txt for full event history.");
    }
}
