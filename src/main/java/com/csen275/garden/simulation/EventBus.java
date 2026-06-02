package com.csen275.garden.simulation;

import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.GardenModule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class EventBus {

    private List<GardenModule> subscribers;
    private LoggingService logger;
    private IntSupplier aliveCountSupplier;

    public EventBus(LoggingService logger) {
        this(logger, () -> 0);
    }

    public EventBus(LoggingService logger, IntSupplier aliveCountSupplier) {
        this.subscribers = new ArrayList<GardenModule>();
        this.logger = logger;
        this.aliveCountSupplier = aliveCountSupplier;
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
                    aliveCountSupplier.getAsInt()
                );
            }
        }
    }

    public void notifyDayStart(int day) {
        for (GardenModule module : subscribers) {
            try {
                module.onDayStart(day);
            } catch (Exception e) {
                logger.log(day, "ERROR", module.getName() + " onDayStart: " + e.getMessage(), aliveCountSupplier.getAsInt());
            }
        }
    }

    public void notifyDayEnd(int day) {
        for (GardenModule module : subscribers) {
            try {
                module.onDayEnd(day);
            } catch (Exception e) {
                logger.log(day, "ERROR", module.getName() + " onDayEnd: " + e.getMessage(), aliveCountSupplier.getAsInt());
            }
        }
    }

    public List<GardenModule> getSubscribers() {
        return subscribers;
    }
}
