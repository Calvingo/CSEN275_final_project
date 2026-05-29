package com.csen275.garden.domain.sensor;

public class Thermometer implements Sensor {

    private String name;
    private int currentTempF;

    public Thermometer(String name) {
        this.name = name;
        this.currentTempF = 72;
    }

    public void setTemp(int tempF) {
        this.currentTempF = tempF;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double read() {
        return currentTempF;
    }
}
