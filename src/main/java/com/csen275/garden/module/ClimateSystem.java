package com.csen275.garden.module;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;

import java.util.List;

public class ClimateSystem implements GardenModule {

    private static final int DEFAULT_TEMP = 72;
    private static final int HOT_THRESHOLD = 95;
    private static final int COLD_THRESHOLD = 50;
    private static final int MIN_TEMP = 40;
    private static final int MAX_TEMP = 120;

    private Garden garden;
    private LoggingService logger;
    private int currentTempF;

    public ClimateSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.currentTempF = DEFAULT_TEMP;
    }

    @Override
    public String getName() {
        return "ClimateSystem";
    }

    @Override
    public void onDayStart(int day) {
        // nothing to do at start of day
    }

    @Override
    public void onDayEnd(int day) {
        applyThermalStress(day);
        resetDaily(day);
    }

    @Override
    public void onEvent(GardenEvent event) {
        if (event.getType() == EventType.TEMPERATURE) {
            setTemperature(event.getDay(), event.getIntValue());
        }
    }

    public void setTemperature(int day, int tempF) {
        if (tempF < MIN_TEMP || tempF > MAX_TEMP) {
            logger.log(day, "ERROR", "ClimateSystem: invalid temperature " + tempF, garden.getLivingCount());
            return;
        }
        currentTempF = tempF;
        logger.log(day, "TEMPERATURE", String.valueOf(tempF), garden.getLivingCount());
    }

    public void applyThermalStress(int day) {
        if (currentTempF <= HOT_THRESHOLD && currentTempF >= COLD_THRESHOLD) {
            return;
        }

        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            if (currentTempF > HOT_THRESHOLD) {
                int stress = (currentTempF - HOT_THRESHOLD) / 5;
                if (stress < 1) {
                    stress = 1;
                }
                plant.applyStress(stress);
            } else if (currentTempF < COLD_THRESHOLD) {
                int stress = (COLD_THRESHOLD - currentTempF) / 5;
                if (stress < 1) {
                    stress = 1;
                }
                plant.applyStress(stress);
            }
        }

        garden.removeDead();
    }

    public void resetDaily(int day) {
        currentTempF = DEFAULT_TEMP;
        logger.log(day, "DAY_END", "temp_reset day=" + day, garden.getLivingCount());
    }

    public int getCurrentTempF() {
        return currentTempF;
    }
}
