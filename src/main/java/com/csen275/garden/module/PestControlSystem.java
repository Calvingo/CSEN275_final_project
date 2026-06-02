package com.csen275.garden.module;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.insect.Parasite;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PestControlSystem implements GardenModule {

    private static final int PARASITE_DAMAGE = 15;
    private static final int CONTROL_RECOVERY = 4;
    private static final int PROACTIVE_RECOVERY = 2;
    private static final int PROACTIVE_HEALTH_THRESHOLD = 40;
    private static final int FOLLOW_UP_DAMAGE = 1;

    private Garden garden;
    private LoggingService logger;
    private List<Parasite> activeParasites;
    private Set<String> treatedPlantIds;

    public PestControlSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.activeParasites = new ArrayList<Parasite>();
        this.treatedPlantIds = new HashSet<String>();
    }

    @Override
    public String getName() {
        return "PestControlSystem";
    }

    @Override
    public void onDayStart(int day) {
        treatedPlantIds.clear();
        runProactiveScan(day);
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
        Parasite parasite = new Parasite(parasiteName, PARASITE_DAMAGE);
        activeParasites.add(parasite);

        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            List<String> vulnerabilities = plant.getType().getParasites();
            if (vulnerabilities.contains(parasite.getName())) {
                plant.applyStress(parasite.getDamage());
            }
        }

        garden.removeDeadAndLog(logger, day);
        logger.log(day, "PARASITE", parasiteName, garden.getLivingCount());

        deployControl(day, parasiteName);
    }

    public void deployControl(int day, String parasiteName) {
        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            List<String> vulnerabilities = plant.getType().getParasites();
            if (vulnerabilities.contains(parasiteName)) {
                plant.applyRecovery(CONTROL_RECOVERY);
                treatedPlantIds.add(plant.getId());
            }
        }

        logger.log(day, "PEST_CONTROL", "treated for " + parasiteName, garden.getLivingCount());
    }

    public void runProactiveScan(int day) {
        int treated = 0;

        for (PlantInstance plant : garden.getLivingPlants()) {
            if (plant.getHealth() < PROACTIVE_HEALTH_THRESHOLD
                && !plant.getType().getParasites().isEmpty()) {
                plant.applyRecovery(PROACTIVE_RECOVERY);
                treatedPlantIds.add(plant.getId());
                treated++;
            }
        }

        if (treated > 0) {
            logger.log(day, "PEST_CONTROL", "proactive_scan treated=" + treated, garden.getLivingCount());
        }
    }

    public void tickInfestations(int day) {
        if (activeParasites.isEmpty()) {
            return;
        }

        List<PlantInstance> plants = garden.getLivingPlants();

        for (Parasite parasite : activeParasites) {
            for (PlantInstance plant : plants) {
                List<String> vulnerabilities = plant.getType().getParasites();
                if (vulnerabilities.contains(parasite.getName())
                    && !treatedPlantIds.contains(plant.getId())) {
                    plant.applyStress(FOLLOW_UP_DAMAGE);
                }
            }
        }

        activeParasites.clear();

        garden.removeDeadAndLog(logger, day);
    }

    public List<String> getActiveParasites() {
        List<String> names = new ArrayList<String>();
        for (Parasite parasite : activeParasites) {
            names.add(parasite.getName());
        }
        return names;
    }
}
