package com.csen275.garden.simulation;

import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.GardenModule;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private List<GardenModule> subscribers;
    private LoggingService logger;

    public EventBus(LoggingService logger) {
        this.subscribers = new ArrayList<GardenModule>();
        this.logger = logger;
    }

    public void subscribe(GardenModule module) {
        subscribers.add(module);
    }

    public void publish(GardenEvent event) {
        for (GardenModule module : subscribers) {
            try {
                module.onEvent(event);
            } catch (Exception e) {
                logger.log(
                    event.getDay(),
                    "ERROR",
                    module.getName() + ": " + e.getMessage(),
                    -1
                );
            }
        }
    }

    public void notifyDayStart(int day) {
        for (GardenModule module : subscribers) {
            try {
                module.onDayStart(day);
            } catch (Exception e) {
                logger.log(day, "ERROR", module.getName() + " onDayStart: " + e.getMessage(), -1);
            }
        }
    }

    public void notifyDayEnd(int day) {
        for (GardenModule module : subscribers) {
            try {
                module.onDayEnd(day);
            } catch (Exception e) {
                logger.log(day, "ERROR", module.getName() + " onDayEnd: " + e.getMessage(), -1);
            }
        }
    }

    public List<GardenModule> getSubscribers() {
        return subscribers;
    }
}
