package com.csen275.garden;

import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;
import com.csen275.garden.module.GardenModule;
import com.csen275.garden.simulation.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private LoggingService logger;
    private EventBus bus;

    @BeforeEach
    void setup() {
        logger = new LoggingService();
        logger.clearLog();
        bus = new EventBus(logger);
    }

    @AfterEach
    void tearDown() {
        logger.clearLog();
    }

    @Test
    void eventDeliveredToAllSubscribers() {
        int[] countA = {0};
        int[] countB = {0};

        GardenModule moduleA = new GardenModule() {
            public String getName() { return "A"; }
            public void onDayStart(int day) {}
            public void onDayEnd(int day) {}
            public void onEvent(GardenEvent event) { countA[0]++; }
        };

        GardenModule moduleB = new GardenModule() {
            public String getName() { return "B"; }
            public void onDayStart(int day) {}
            public void onDayEnd(int day) {}
            public void onEvent(GardenEvent event) { countB[0]++; }
        };

        bus.subscribe(moduleA);
        bus.subscribe(moduleB);

        bus.publish(new GardenEvent(EventType.RAIN, 1, "25", 25));

        assertEquals(1, countA[0]);
        assertEquals(1, countB[0]);
    }

    @Test
    void exceptionInOneModuleDoesNotCrashBus() {
        int[] countSafe = {0};

        GardenModule crasher = new GardenModule() {
            public String getName() { return "Crasher"; }
            public void onDayStart(int day) {}
            public void onDayEnd(int day) {}
            public void onEvent(GardenEvent event) { throw new RuntimeException("crash"); }
        };

        GardenModule safe = new GardenModule() {
            public String getName() { return "Safe"; }
            public void onDayStart(int day) {}
            public void onDayEnd(int day) {}
            public void onEvent(GardenEvent event) { countSafe[0]++; }
        };

        bus.subscribe(crasher);
        bus.subscribe(safe);

        assertDoesNotThrow(() -> bus.publish(new GardenEvent(EventType.RAIN, 1, "10", 10)));
        assertEquals(1, countSafe[0]);
    }

    @Test
    void dayStartAndEndNotifyAllModules() {
        int[] starts = {0};
        int[] ends = {0};

        GardenModule module = new GardenModule() {
            public String getName() { return "Tracker"; }
            public void onDayStart(int day) { starts[0]++; }
            public void onDayEnd(int day) { ends[0]++; }
            public void onEvent(GardenEvent event) {}
        };

        bus.subscribe(module);
        bus.notifyDayStart(1);
        bus.notifyDayEnd(1);

        assertEquals(1, starts[0]);
        assertEquals(1, ends[0]);
    }
}
