package com.csen275.garden;

import com.csen275.garden.domain.garden.Garden;
import com.csen275.garden.domain.garden.GardenGrid;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.domain.plant.PlantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GardenGridTest {

    private PlantType rose;
    private PlantType tomato;

    @BeforeEach
    void setup() {
        rose = new PlantType("Rose", 10, 2, List.of("aphid"));
        tomato = new PlantType("Tomato", 15, 3, List.of("hornworm"));
    }

    @Test
    void canPlace12PlantsOnGrid() {
        Garden garden = new Garden();
        for (int i = 0; i < 12; i++) {
            boolean placed = garden.placePlantOnGrid(rose.createInstance());
            assertTrue(placed, "Plant " + i + " should be placed");
        }
        assertEquals(12, garden.getLivingCount());
    }

    @Test
    void deadPlantRemovedFromLivingList() {
        Garden garden = new Garden();
        PlantInstance p = rose.createInstance();
        garden.placePlantOnGrid(p);
        assertEquals(1, garden.getLivingCount());

        p.applyStress(100);
        garden.removeDead();
        assertEquals(0, garden.getLivingCount());
    }

    @Test
    void removedPlantClearedFromGrid() {
        Garden garden = new Garden();
        PlantInstance p = rose.createInstance();
        garden.placePlantOnGrid(p);

        p.applyStress(100);
        garden.removeDead();

        for (Plot plot : garden.getGrid().getAllPlots()) {
            assertNull(plot.getPlant());
        }
    }

    @Test
    void plotApplyWaterFeedsPlant() {
        Plot plot = new Plot();
        PlantInstance p = tomato.createInstance();
        plot.setPlant(p);
        int before = p.getWaterLevel();
        plot.applyWater(30);
        assertEquals(before + 30, p.getWaterLevel());
        assertEquals(50, plot.getSoilMoisture()); // started at 20, added 30
    }

    @Test
    void plotTickDayDrainsMoisture() {
        Plot plot = new Plot();
        plot.applyWater(50);
        int before = plot.getSoilMoisture();
        plot.tickDay();
        assertTrue(plot.getSoilMoisture() < before);
    }

    @Test
    void gridReturnsNullForOutOfBounds() {
        GardenGrid grid = new GardenGrid(4, 4);
        assertNull(grid.getPlot(-1, 0));
        assertNull(grid.getPlot(0, 10));
    }
}
