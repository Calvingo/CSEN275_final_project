package com.csen275.garden.simulation;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.GardenModule;

import java.util.List;

public class SimulationEngine {

    private Garden garden;
    private LoggingService logger;
    private SimulationClock clock;
    private EventBus eventBus;

    public SimulationEngine(Garden garden, LoggingService logger, List<GardenModule> modules) {
        this.garden = garden;
        this.logger = logger;
        this.clock = new SimulationClock();
        this.eventBus = new EventBus(logger);

        for (GardenModule module : modules) {
            eventBus.subscribe(module);
        }
    }

    public void start() {
        logger.log(clock.getCurrentDay(), "INIT", "config_loaded", garden.getLivingCount());
    }

    public void tickHour() {
        int day = clock.getCurrentDay();

        try {
            eventBus.notifyDayStart(day);
        } catch (Exception e) {
            logger.log(day, "ERROR", "DAY_START: " + e.getMessage(), garden.getLivingCount());
        }

        try {
            garden.tickDay();
        } catch (Exception e) {
            logger.log(day, "ERROR", "TICK_DAY: " + e.getMessage(), garden.getLivingCount());
        }

        try {
            eventBus.notifyDayEnd(day);
        } catch (Exception e) {
            logger.log(day, "ERROR", "DAY_END: " + e.getMessage(), garden.getLivingCount());
        }

        clock.incrementDay();
    }

    public void onRain(int amount) {
        int day = clock.getCurrentDay();
        GardenEvent event = new GardenEvent(EventType.RAIN, day, String.valueOf(amount), amount);
        eventBus.publish(event);
    }

    public void onTemperature(int fahrenheit) {
        int day = clock.getCurrentDay();
        GardenEvent event = new GardenEvent(EventType.TEMPERATURE, day, String.valueOf(fahrenheit), fahrenheit);
        eventBus.publish(event);
    }

    public void onParasite(String name) {
        int day = clock.getCurrentDay();
        GardenEvent event = new GardenEvent(EventType.PARASITE, day, name, 0);
        eventBus.publish(event);
    }

    public int getCurrentDay() {
        return clock.getCurrentDay();
    }

    public Garden getGarden() {
        return garden;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
