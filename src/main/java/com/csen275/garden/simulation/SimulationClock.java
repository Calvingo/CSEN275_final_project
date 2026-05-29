package com.csen275.garden.simulation;

public class SimulationClock {

    private int currentDay;

    public SimulationClock() {
        this.currentDay = 0;
    }

    public void incrementDay() {
        currentDay = currentDay + 1;
    }

    public int getCurrentDay() {
        return currentDay;
    }
}
