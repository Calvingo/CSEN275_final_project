package com.csen275.garden.module;

import com.csen275.garden.event.GardenEvent;

public interface GardenModule {

    String getName();

    void onDayStart(int day);

    void onDayEnd(int day);

    void onEvent(GardenEvent event);
}
