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
    private boolean heatingActive;
    private boolean coolingActive;

    // Snapshot of the most recent day's climate, kept past the daily reset so the UI
    // can show what actually happened on the latest tick.
    private int lastTempF;
    private boolean lastHeatingActive;
    private boolean lastCoolingActive;

    public ClimateSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.currentTempF = DEFAULT_TEMP;
        this.lastTempF = DEFAULT_TEMP;
        this.heatingActive = false;
        this.coolingActive = false;
    }

    @Override
    public String getName() {
        return "ClimateSystem";
    }

    @Override
    public void onDayStart(int day) {
        // Temperature is applied via events and reset at day end.
    }

    @Override
    public void onDayEnd(int day) {
        applyThermalStress(day);
        // Preserve what this day looked like before the reset wipes it, so status stays current.
        lastTempF = currentTempF;
        lastHeatingActive = heatingActive;
        lastCoolingActive = coolingActive;
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
        heatingActive = tempF < COLD_THRESHOLD;
        coolingActive = tempF > HOT_THRESHOLD;

        logger.log(day, "TEMPERATURE", String.valueOf(tempF), garden.getLivingCount());

        if (heatingActive) {
            logger.log(day, "CLIMATE_CONTROL", "heating_active temp=" + tempF, garden.getLivingCount());
        } else if (coolingActive) {
            logger.log(day, "CLIMATE_CONTROL", "cooling_active temp=" + tempF, garden.getLivingCount());
        }
    }

    public void applyThermalStress(int day) {
        if (currentTempF <= HOT_THRESHOLD && currentTempF >= COLD_THRESHOLD) {
            return;
        }

        List<PlantInstance> plants = garden.getLivingPlants();

        for (PlantInstance plant : plants) {
            if (currentTempF > HOT_THRESHOLD) {
                // Heat stress scales with how far above the hot threshold we are. The base is
                // tuned so that — even after climate control and same-day natural recovery —
                // every plant type takes a meaningful net hit (heal rates top out at 4).
                int stress = 4 + (currentTempF - HOT_THRESHOLD) / 5;   // 96°F->4, 105°F->6, 120°F->9
                if (coolingActive) {
                    stress -= 1;                                       // climate control shaves a little off
                }
                stress = Math.max(2, Math.min(5, stress));            // net 2-5 HP per heat wave
                plant.applyStress(stress);
            } else if (currentTempF < COLD_THRESHOLD) {
                int stress = (COLD_THRESHOLD - currentTempF) / 5;
                if (stress < 1) {
                    stress = 1;
                }
                if (heatingActive) {
                    stress = Math.max(1, stress / 2);
                }
                plant.applyStress(stress);
            }
        }

        garden.removeDeadAndLog(logger, day);
    }

    public void resetDaily(int day) {
        currentTempF = DEFAULT_TEMP;
        heatingActive = false;
        coolingActive = false;
        logger.log(day, "DAY_END", "temp_reset day=" + day, garden.getLivingCount());
    }

    public int getCurrentTempF() {
        return currentTempF;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public boolean isCoolingActive() {
        return coolingActive;
    }

    public int getLastTempF() {
        return lastTempF;
    }

    public boolean isLastHeatingActive() {
        return lastHeatingActive;
    }

    public boolean isLastCoolingActive() {
        return lastCoolingActive;
    }
}
