package com.csen275.garden.domain.insect;

public class Parasite extends Insect {

    private final int damage;

    public Parasite(String name, int damage) {
        super(name);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }
}
