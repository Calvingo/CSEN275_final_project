package com.csen275.garden.app;

import com.csen275.garden.ui.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class GardenApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();
        controller.start(primaryStage);
    }
}
