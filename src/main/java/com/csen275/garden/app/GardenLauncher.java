package com.csen275.garden.app;

import javafx.application.Application;

/**
 * Launcher entry point for IDE and classpath runs.
 * The main class must not extend {@link Application} so JavaFX modules load correctly.
 */
public class GardenLauncher {

    public static void main(String[] args) {
        Application.launch(GardenApp.class, args);
    }
}
