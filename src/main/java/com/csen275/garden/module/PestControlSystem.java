package com.csen275.garden.module;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;

import java.util.ArrayList;
import java.util.List;

public class PestControlSystem implements GardenModule {

    private static final int PARASITE_DAMAGE = 15;
    private static final int CONTROL_DAMAGE_REDUCTION = 5;

    private Garden garden;
    private LoggingService logger;
    private List<String> activeParasites;

    public PestControlSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.activeParasites = new ArrayList<String>();
    }

    @Override
    public String getName() {
        return "PestControlSystem";
    }

    @Override
    public void onDayStart(int day) {
        // nothing to do at start of day
    }

    @Override
    public void onDayEnd(int day) {
        tickInfestations(day);
    }

    @Override
    public void onEvent(GardenEvent event) {
        if (event.getType() == EventType.PARASITE) {
            triggerParasite(event.getDay(), event.getPayload());
        }
    }

    public void triggerParasite(int day, String parasiteName) {
        activeParasites.add(parasiteName);

        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            List<String> vulnerabilities = plant.getType().getParasites();
            if (vulnerabilities.contains(parasiteName)) {
                plant.applyStress(PARASITE_DAMAGE);
            }
        }

        garden.removeDead();
        logger.log(day, "PARASITE", parasiteName, garden.getLivingCount());

        deployControl(day, parasiteName);
    }

    public void deployControl(int day, String parasiteName) {
        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            List<String> vulnerabilities = plant.getType().getParasites();
            if (vulnerabilities.contains(parasiteName)) {
                int currentHealth = plant.getHealth();
                // Control reduces ongoing damage but does NOT restore health to 100
                if (currentHealth < 95) {
                    plant.applyWater(5);
                }
            }
        }

        logger.log(day, "PEST_CONTROL", "deployed for " + parasiteName, garden.getLivingCount());
    }

    public void tickInfestations(int day) {
        if (activeParasites.isEmpty()) {
            return;
        }

        List<PlantInstance> plants = garden.getLivingPlants();

        for (String parasite : activeParasites) {
            for (PlantInstance plant : plants) {
                List<String> vulnerabilities = plant.getType().getParasites();
                if (vulnerabilities.contains(parasite)) {
                    plant.applyStress(CONTROL_DAMAGE_REDUCTION);
                }
            }
        }

        // Clear parasites after one more tick so they don't persist forever
        activeParasites.clear();

        garden.removeDead();
    }

    public List<String> getActiveParasites() {
        return activeParasites;
    }
}
