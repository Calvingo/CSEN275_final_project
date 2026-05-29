package com.csen275.garden;

import com.csen275.garden.domain.plant.GrowthStage;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.domain.plant.PlantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantTest {

    private PlantType roseType;
    private PlantInstance rose;

    @BeforeEach
    void setup() {
        roseType = new PlantType("Rose", 10, 2, List.of("aphid", "spider_mite"));
        rose = roseType.createInstance();
    }

    @Test
    void plantStartsAliveWithFullHealth() {
        assertTrue(rose.isAlive());
        assertEquals(100, rose.getHealth());
    }

    @Test
    void healthZeroKillsPlant() {
        rose.applyStress(100);
        assertFalse(rose.isAlive());
        assertEquals(GrowthStage.DEAD, rose.getStage());
    }

    @Test
    void applyWaterIncreasesWaterLevel() {
        rose.applyWater(20);
        assertEquals(20, rose.getWaterLevel());
    }

    @Test
    void waterLevelCapsAt100() {
        rose.applyWater(200);
        assertEquals(100, rose.getWaterLevel());
    }

    @Test
    void naturalRecoveryRestoresHealth() {
        rose.applyStress(20);
        int healthBefore = rose.getHealth();
        rose.applyWater(50);
        rose.tickNaturalRecovery();
        assertTrue(rose.getHealth() > healthBefore);
    }

    @Test
    void noRecoveryWhenUnderwatered() {
        rose.applyStress(20);
        int healthBefore = rose.getHealth();
        rose.tickNaturalRecovery();
        assertEquals(healthBefore, rose.getHealth());
    }

    @Test
    void deadPlantDoesNotRecover() {
        rose.applyStress(100);
        rose.applyWater(100);
        rose.tickNaturalRecovery();
        assertFalse(rose.isAlive());
    }

    @Test
    void tickDailyReducesWaterAndStressesIfDry() {
        int healthBefore = rose.getHealth();
        for (int i = 0; i < 5; i++) {
            rose.tickDaily();
        }
        assertTrue(rose.getHealth() < healthBefore);
    }

    @Test
    void createInstanceReturnsUniqueIds() {
        PlantInstance a = roseType.createInstance();
        PlantInstance b = roseType.createInstance();
        assertNotEquals(a.getId(), b.getId());
    }
}
