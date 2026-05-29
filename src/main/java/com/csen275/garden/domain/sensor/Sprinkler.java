package com.csen275.garden.domain.sensor;

import com.csen275.garden.domain.garden.Plot;

public class Sprinkler {

    private String name;
    private Plot plot;
    private boolean active;

    public Sprinkler(String name, Plot plot) {
        this.name = name;
        this.plot = plot;
        this.active = false;
    }

    public void activate(int waterAmount) {
        active = true;
        plot.applyWater(waterAmount);
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() { return active; }
    public String getName() { return name; }
}
