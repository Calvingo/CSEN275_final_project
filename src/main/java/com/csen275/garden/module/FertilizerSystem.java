package com.csen275.garden.module;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.event.EventType;
import com.csen275.garden.event.GardenEvent;
import com.csen275.garden.logging.LoggingService;

public class FertilizerSystem implements GardenModule {

    private static final int LOW_NUTRIENT_THRESHOLD = 25;
    private static final int FERTILIZER_AMOUNT = 20;
    private static final int PARASITE_NUTRIENT_BOOST = 10;
    private static final int LOW_PLOT_WARNING_THRESHOLD = 5;

    private Garden garden;
    private LoggingService logger;
    private int plotsTreatedToday;

    public FertilizerSystem(Garden garden, LoggingService logger) {
        this.garden = garden;
        this.logger = logger;
        this.plotsTreatedToday = 0;
    }

    @Override
    public String getName() {
        return "FertilizerSystem";
    }

    @Override
    public void onDayStart(int day) {
        plotsTreatedToday = 0;
        logNutrientCheck(day);
    }

    @Override
    public void onDayEnd(int day) {
        fertilizeLowPlots(day);
    }

    @Override
    public void onEvent(GardenEvent event) {
        if (event.getType() == EventType.PARASITE) {
            boostNutrientsAfterParasite(event.getDay());
        }
    }

    public void fertilizeLowPlots(int day) {
        for (int row = 0; row < garden.getGrid().getRows(); row++) {
            for (int col = 0; col < garden.getGrid().getCols(); col++) {
                Plot plot = garden.getGrid().getPlot(row, col);
                if (plot.getPlant() != null
                    && plot.getPlant().isAlive()
                    && plot.getNutrientLevel() < LOW_NUTRIENT_THRESHOLD) {
                    plot.applyFertilizer(FERTILIZER_AMOUNT);
                    plotsTreatedToday++;
                    logger.log(
                        day,
                        "FERTILIZER",
                        "plot(" + row + "," + col + ") nutrients=" + plot.getNutrientLevel(),
                        garden.getLivingCount()
                    );
                }
            }
        }
    }

    public void boostNutrientsAfterParasite(int day) {
        for (int row = 0; row < garden.getGrid().getRows(); row++) {
            for (int col = 0; col < garden.getGrid().getCols(); col++) {
                Plot plot = garden.getGrid().getPlot(row, col);
                if (plot.getPlant() != null && plot.getPlant().isAlive()) {
                    plot.applyFertilizer(PARASITE_NUTRIENT_BOOST);
                }
            }
        }
        logger.log(day, "FERTILIZER", "parasite_recovery_boost", garden.getLivingCount());
    }

    public void applyManualFertilizer(int day) {
        for (int row = 0; row < garden.getGrid().getRows(); row++) {
            for (int col = 0; col < garden.getGrid().getCols(); col++) {
                Plot plot = garden.getGrid().getPlot(row, col);
                if (plot.getPlant() != null && plot.getPlant().isAlive()) {
                    plot.applyFertilizer(FERTILIZER_AMOUNT);
                    plotsTreatedToday++;
                }
            }
        }
        logger.log(day, "MANUAL_FERTILIZER", "user_triggered", garden.getLivingCount());
    }

    private void logNutrientCheck(int day) {
        int lowCount = 0;

        for (Plot plot : garden.getGrid().getAllPlots()) {
            if (plot.getPlant() != null
                && plot.getPlant().isAlive()
                && plot.getNutrientLevel() < LOW_NUTRIENT_THRESHOLD) {
                lowCount++;
            }
        }

        if (lowCount >= LOW_PLOT_WARNING_THRESHOLD) {
            logger.log(day, "NUTRIENT_CHECK", "low_plots=" + lowCount, garden.getLivingCount());
        }
    }

    public int getPlotsTreatedToday() {
        return plotsTreatedToday;
    }
}
