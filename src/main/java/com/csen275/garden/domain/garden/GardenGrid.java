package com.csen275.garden.domain.garden;

import com.csen275.garden.domain.plant.PlantInstance;

import java.util.ArrayList;
import java.util.List;

public class GardenGrid {

    private int rows;
    private int cols;
    private Plot[][] plots;

    public GardenGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.plots = new Plot[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                plots[r][c] = new Plot();
            }
        }
    }

    public boolean placePlant(PlantInstance plant, int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        if (plots[row][col].getPlant() != null) {
            return false;
        }
        plots[row][col].setPlant(plant);
        return true;
    }

    public Plot getPlot(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return null;
        }
        return plots[row][col];
    }

    public List<Plot> getAllPlots() {
        List<Plot> all = new ArrayList<Plot>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                all.add(plots[r][c]);
            }
        }
        return all;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
