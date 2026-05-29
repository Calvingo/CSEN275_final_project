package com.csen275.garden.domain.plant;

public class PlantInstance {

    private String id;
    private PlantType type;
    private int health;
    private int waterLevel;
    private GrowthStage stage;
    private boolean alive;

    public PlantInstance(String id, PlantType type) {
        this.id = id;
        this.type = type;
        this.health = 100;
        this.waterLevel = 0;
        this.stage = GrowthStage.GROWING;
        this.alive = true;
    }

    public void applyStress(int amount) {
        if (!alive) {
            return;
        }
        health = health - amount;
        if (health <= 0) {
            health = 0;
            alive = false;
            stage = GrowthStage.DEAD;
        } else if (health < 30) {
            stage = GrowthStage.DYING;
        } else if (health < 60) {
            stage = GrowthStage.STRESSED;
        }
    }

    public void applyWater(int amount) {
        if (!alive) {
            return;
        }
        waterLevel = waterLevel + amount;
        if (waterLevel > 100) {
            waterLevel = 100;
        }
    }

    public void tickNaturalRecovery() {
        if (!alive) {
            return;
        }
        if (waterLevel >= type.getWaterRequirement() && health < 100) {
            health = health + type.getHealRate();
            if (health > 100) {
                health = 100;
            }
            if (stage == GrowthStage.DYING || stage == GrowthStage.STRESSED) {
                stage = GrowthStage.RECOVERING;
            }
        }
    }

    public void tickDaily() {
        if (!alive) {
            return;
        }
        // Plants lose water each day; if under-watered they take stress
        waterLevel = waterLevel - 5;
        if (waterLevel < 0) {
            waterLevel = 0;
        }
        if (waterLevel < type.getWaterRequirement()) {
            applyStress(3);
        } else {
            tickNaturalRecovery();
        }
        // Update stage based on health
        if (alive) {
            if (health >= 80) {
                stage = GrowthStage.MATURE;
            } else if (health >= 60) {
                stage = GrowthStage.GROWING;
            } else if (health >= 30) {
                stage = GrowthStage.STRESSED;
            } else {
                stage = GrowthStage.DYING;
            }
        }
    }

    public String getId() { return id; }
    public PlantType getType() { return type; }
    public int getHealth() { return health; }
    public int getWaterLevel() { return waterLevel; }
    public GrowthStage getStage() { return stage; }
    public boolean isAlive() { return alive; }
}
