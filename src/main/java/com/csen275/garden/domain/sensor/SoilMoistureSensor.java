package com.csen275.garden.domain.sensor;

import com.csen275.garden.domain.garden.Plot;

public class SoilMoistureSensor implements Sensor {

    private String name;
    private Plot plot;

    public SoilMoistureSensor(String name, Plot plot) {
        this.name = name;
        this.plot = plot;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double read() {
        return plot.getSoilMoisture();
    }
}
