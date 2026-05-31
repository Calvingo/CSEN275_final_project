package com.csen275.garden.module;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;

import java.util.List;

public class WateringSystem implements GardenModule {

    private static final int DRY_THRESHOLD = 20;
    private static final int SPRINKLER_WATER_AMOUNT = 15;

    private Garden garden;
    private LoggingService logger;
    private boolean rainDuringCurrentDay;

    public WateringSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.rainDuringCurrentDay = false;
    }

    @Override
    public String getName() {
        return "WateringSystem";
    }

    @Override
    public void onDayStart(int day) {
        // Rain tracking is cleared at the end of the previous day in onDayEnd.
    }

    @Override
    public void onDayEnd(int day) {
        activateSprinklers(day);

        if (!rainDuringCurrentDay) {
            resetDailyMoisture();
        }

        rainDuringCurrentDay = false;
    }

    @Override
    public void onEvent(GardenEvent event) {
        if (event.getType() == EventType.RAIN) {
            handleRain(event.getDay(), event.getIntValue());
        }
    }

    public void handleRain(int day, int amount) {
        rainDuringCurrentDay = true;

        List<Plot> plots = garden.getGrid().getAllPlots();

        for (Plot plot : plots) {
            plot.applyWater(amount);
        }

        logger.log(day, "RAIN", String.valueOf(amount), garden.getLivingCount());
    }

    public void activateSprinklers(int day) {
        List<Plot> plots = garden.getGrid().getAllPlots();

        for (int row = 0; row < garden.getGrid().getRows(); row++) {
            for (int col = 0; col < garden.getGrid().getCols(); col++) {
                Plot plot = garden.getGrid().getPlot(row, col);
                if (plot.getSoilMoisture() < DRY_THRESHOLD) {
                    plot.applyWater(SPRINKLER_WATER_AMOUNT);
                    logger.log(
                        day,
                        "SPRINKLER",
                        "plot(" + row + "," + col + ") water=" + SPRINKLER_WATER_AMOUNT,
                        garden.getLivingCount()
                    );
                }
            }
        }
    }

    public void resetDailyMoisture() {
        List<Plot> plots = garden.getGrid().getAllPlots();

        for (Plot plot : plots) {
            if (plot.getSoilMoisture() > 30) {
                plot.setSoilMoisture(30);
            }
        }
    }

    public boolean isRainedToday() {
        return rainDuringCurrentDay;
    }
}
