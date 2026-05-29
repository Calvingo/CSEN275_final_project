package com.csen275.garden.event;

public class GardenEvent {

    private EventType type;
    private int day;
    private String payload;
    private int intValue;

    public GardenEvent(EventType type, int day, String payload, int intValue) {
        this.type = type;
        this.day = day;
        this.payload = payload;
        this.intValue = intValue;
    }

    public EventType getType() {
        return type;
    }

    public int getDay() {
        return day;
    }

    public String getPayload() {
        return payload;
    }

    public int getIntValue() {
        return intValue;
    }
}
