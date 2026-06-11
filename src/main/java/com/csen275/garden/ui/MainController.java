package com.csen275.garden.ui;

import com.csen275.garden.domain.garden.GardenGrid;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.module.ClimateSystem;
import com.csen275.garden.module.WateringSystem;
import com.csen275.garden.simulation.EnvironmentEventGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MainController {

    private static final int LOG_TAIL_LINES = 50;
    private static final String USER_MANUAL_PATH = "docs/user-manual.md";

    // Every parasite the simulation understands; each only harms plants susceptible to it.
    private static final String[] PARASITE_TYPES = {
        "aphid", "spider_mite", "thrip", "whitefly", "hornworm", "bark_beetle", "slug"
    };

    // Nature-themed sky-to-grass backdrop for the whole window.
    private static final String ROOT_BACKGROUND =
        "-fx-background-color: linear-gradient(to bottom, #add8f0 0%, #cfe8b0 45%, #8bc34a 100%);";

    // Shared toolbar/panel card styling.
    private static final String BAR_STYLE =
        "-fx-background-color: rgba(46, 78, 32, 0.82); -fx-background-radius: 10; -fx-padding: 8;";
    private static final String ACTION_BUTTON_STYLE =
        "-fx-background-color: linear-gradient(to bottom, #66bb6a, #43a047); -fx-text-fill: white; "
        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
    private static final String PARASITE_BUTTON_STYLE =
        "-fx-background-color: linear-gradient(to bottom, #ef7d6a, #c0392b); -fx-text-fill: white; "
        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
    private static final String CARD_STYLE =
        "-fx-background-color: rgba(255, 252, 245, 0.92); -fx-background-radius: 10; "
        + "-fx-border-color: #8d6e63; -fx-border-width: 1.5; -fx-border-radius: 10;";
    // Light backing so the speed slider's tick marks and number labels stay readable.
    private static final String SPEED_CHIP_STYLE =
        "-fx-background-color: rgba(255, 252, 245, 0.92); -fx-background-radius: 8; -fx-padding: 2 10 2 10;";

    private final GardenUiSession session = new GardenUiSession();
    private final Random random = new Random();

    private Stage stage;
    private GridPane gardenGrid;
    private Label dayLabel;
    private Label aliveLabel;
    private Label wateringStatus;
    private Label climateStatus;
    private Label pestStatus;
    private Label fertilizerStatus;
    private TextArea logArea;
    private Button startPauseButton;
    private Timeline simulationTimeline;
    private boolean simulationRunning;

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("CSEN275 Garden Simulation");
        stage.setScene(new Scene(buildRoot(), 980, 720));
        stage.show();
        refreshActionStates();
    }

    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle(ROOT_BACKGROUND);
        root.setTop(buildToolbar());
        root.setCenter(buildGardenView());
        root.setRight(buildStatusPanel());
        root.setBottom(buildLogPanel());
        return root;
    }

    private VBox buildToolbar() {
        Button initializeButton = actionButton("Initialize", e -> handleInitialize());

        startPauseButton = actionButton("Start", e -> toggleSimulation());
        startPauseButton.setTooltip(new Tooltip("Auto-advance days with random rain, heat waves, or parasite outbreaks"));

        Button manualWaterButton = actionButton("Manual Water", e -> handleManualWater());
        Button manualFertilizerButton = actionButton("Apply Fertilizer", e -> handleManualFertilizer());
        Button addPlantButton = actionButton("Add Plant", e -> handleAddPlant());
        Button rainButton = actionButton("Rain (15)", e -> handleRain(15));
        Button heatButton = actionButton("Heat (105°F)", e -> handleTemperature(105));
        Button stateButton = actionButton("Log State", e -> handleLogState());
        Button helpButton = actionButton("Help", e -> showHelp());

        dayLabel = new Label("Day: —");
        aliveLabel = new Label("Plants alive: —");
        dayLabel.setTextFill(Color.WHITE);
        aliveLabel.setTextFill(Color.WHITE);
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        aliveLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label speedLabel = new Label("Speed:");
        speedLabel.setTextFill(Color.web("#2e4e20"));
        speedLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        Slider speedSlider = new Slider(0.5, 4.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setMinorTickCount(1);
        speedSlider.setBlockIncrement(0.5);
        speedSlider.setPrefWidth(170);
        // Dark tick marks/labels are unreadable on the dark toolbar, so seat the speed
        // control on a light chip where the default dark text shows clearly.
        speedSlider.setStyle("-fx-tick-label-fill: #2e4e20; -fx-text-fill: #2e4e20;");
        HBox speedBox = new HBox(6, speedLabel, speedSlider);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        speedBox.setStyle(SPEED_CHIP_STYLE);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateSimulationSpeed(newVal.doubleValue()));

        Region spacer = new Region();
        HBox mainBar = new HBox(8,
            initializeButton, startPauseButton, manualWaterButton, manualFertilizerButton, addPlantButton,
            rainButton, heatButton, stateButton, helpButton,
            spacer, dayLabel, aliveLabel, speedBox
        );
        mainBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // One button per parasite; each only damages plants susceptible to that pest.
        Label parasiteLabel = new Label("Parasites:");
        parasiteLabel.setTextFill(Color.WHITE);
        parasiteLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        HBox parasiteBar = new HBox(6, parasiteLabel);
        parasiteBar.setAlignment(Pos.CENTER_LEFT);
        for (final String parasite : PARASITE_TYPES) {
            Button button = new Button(parasite);
            button.setStyle(PARASITE_BUTTON_STYLE);
            button.setTooltip(new Tooltip("Trigger a " + parasite + " outbreak (only susceptible plants are harmed)"));
            button.setOnAction(e -> handleParasite(parasite));
            parasiteBar.getChildren().add(button);
        }

        VBox toolbar = new VBox(8, mainBar, parasiteBar);
        toolbar.setStyle(BAR_STYLE);
        return toolbar;
    }

    private Button actionButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle(ACTION_BUTTON_STYLE);
        button.setOnAction(handler);
        return button;
    }

    private ScrollPane buildGardenView() {
        gardenGrid = new GridPane();
        gardenGrid.setHgap(6);
        gardenGrid.setVgap(6);
        gardenGrid.setPadding(new Insets(10));
        gardenGrid.setAlignment(Pos.CENTER);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                StackPane cell = createEmptyCell(row, col);
                gardenGrid.add(cell, col, row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(gardenGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        // Let the nature background show through instead of the default gray viewport.
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private StackPane createEmptyCell(int row, int col) {
        Label label = new Label("empty plot");
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#f0e6d2"));

        StackPane cell = new StackPane(label);
        cell.setMinSize(110, 90);
        cell.setPrefSize(110, 90);
        cell.setStyle(EMPTY_CELL_STYLE);
        cell.setUserData(new int[] { row, col });
        return cell;
    }

    // Brown tilled-soil look for an empty plot.
    private static final String EMPTY_CELL_STYLE =
        "-fx-background-color: linear-gradient(to bottom, #8b5a2b, #5d3a1a); "
        + "-fx-border-color: #4a2e15; -fx-border-width: 1.5; -fx-background-radius: 6; -fx-border-radius: 6;";

    private VBox buildStatusPanel() {
        wateringStatus = new Label("Watering: not initialized");
        climateStatus = new Label("Climate: not initialized");
        pestStatus = new Label("Pest control: not initialized");
        fertilizerStatus = new Label("Fertilizer: not initialized");

        wateringStatus.setWrapText(true);
        climateStatus.setWrapText(true);
        pestStatus.setWrapText(true);
        fertilizerStatus.setWrapText(true);

        Label title = new Label("🌻 Subsystems");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox panel = new VBox(12, title, wateringStatus, climateStatus, pestStatus, fertilizerStatus);
        panel.setPadding(new Insets(14));
        panel.setMinWidth(220);
        panel.setMaxWidth(260);
        panel.setStyle(CARD_STYLE);
        BorderPane.setMargin(panel, new Insets(0, 0, 0, 10));
        return panel;
    }

    private VBox buildLogPanel() {
        Label logTitle = new Label("Event log (last " + LOG_TAIL_LINES + " lines from log.txt)");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setWrapText(false);
        logArea.setStyle("-fx-font-family: monospace;");

        VBox panel = new VBox(6, logTitle, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        panel.setPadding(new Insets(12));
        panel.setStyle(CARD_STYLE);
        BorderPane.setMargin(panel, new Insets(10, 0, 0, 0));
        return panel;
    }

    private void handleInitialize() {
        stopSimulation();
        try {
            session.initialize();
            refreshAll();
            showInfo("Garden initialized from config/garden_config.json.");
        } catch (Exception e) {
            showError("Failed to initialize garden: " + e.getMessage());
        }
    }

    private void toggleSimulation() {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        if (simulationRunning) {
            stopSimulation();
        } else {
            startSimulation(1.0);
        }
    }

    private void startSimulation(double speedMultiplier) {
        double seconds = 2.0 / speedMultiplier;
        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> advanceDay()));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
        simulationRunning = true;
        startPauseButton.setText("Pause");
    }

    private void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
            simulationTimeline = null;
        }
        simulationRunning = false;
        if (startPauseButton != null) {
            startPauseButton.setText("Start");
        }
    }

    private void updateSimulationSpeed(double speedMultiplier) {
        if (simulationRunning) {
            stopSimulation();
            startSimulation(speedMultiplier);
        }
    }

    private void advanceDay() {
        if (!session.isInitialized()) {
            return;
        }
        try {
            EnvironmentEventGenerator.applyRandomEvent(session.getEngine(), random);
            session.getEngine().tickHour();
            refreshAll();
        } catch (Exception e) {
            session.getLogger().log(
                session.getEngine().getCurrentDay(),
                "ERROR",
                "UI tick: " + e.getMessage(),
                session.getGarden().getLivingCount()
            );
            refreshLog();
            showError("Simulation tick failed: " + e.getMessage());
            stopSimulation();
        }
    }

    private void handleManualWater() {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        int day = session.getEngine().getCurrentDay();
        session.getWateringSystem().activateSprinklers(day);
        session.getLogger().log(day, "MANUAL_WATER", "user_triggered", session.getGarden().getLivingCount());
        refreshAll();
    }

    private void handleManualFertilizer() {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        int day = session.getEngine().getCurrentDay();
        session.getFertilizerSystem().applyManualFertilizer(day);
        refreshAll();
    }

    private void handleAddPlant() {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        Optional<PlantInstance> plant = AddPlantDialog.show();
        if (plant.isEmpty()) {
            return;
        }
        boolean placed = session.getGarden().placePlantOnGrid(plant.get());
        if (!placed) {
            showError("No empty plot available on the grid.");
            return;
        }
        refreshAll();
    }

    private void handleRain(int amount) {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        session.getEngine().onRain(amount);
        session.getEngine().tickHour();
        refreshAll();
    }

    private void handleTemperature(int fahrenheit) {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        session.getEngine().onTemperature(fahrenheit);
        session.getEngine().tickHour();
        refreshAll();
    }

    private void handleParasite(String name) {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        session.getEngine().onParasite(name);
        session.getEngine().tickHour();
        refreshAll();
    }

    private void handleLogState() {
        if (!session.isInitialized()) {
            showError("Initialize the garden first.");
            return;
        }
        session.getLogger().logState(session.getEngine().getCurrentDay(), session.getGarden());
        refreshLog();
    }

    private void refreshAll() {
        refreshGrid();
        refreshStatus();
        refreshHeader();
        refreshLog();
        refreshActionStates();
    }

    private void refreshGrid() {
        if (!session.isInitialized()) {
            return;
        }
        GardenGrid grid = session.getGarden().getGrid();
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                Plot plot = grid.getPlot(row, col);
                StackPane cell = findCell(row, col);
                if (cell == null) {
                    continue;
                }
                updateCell(cell, plot, row, col);
            }
        }
    }

    private StackPane findCell(int row, int col) {
        for (javafx.scene.Node node : gardenGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                int[] coords = (int[]) cell.getUserData();
                if (coords[0] == row && coords[1] == col) {
                    return cell;
                }
            }
        }
        return null;
    }

    private void updateCell(StackPane cell, Plot plot, int row, int col) {
        PlantInstance plant = plot.getPlant();
        Label label = (Label) cell.getChildren().get(0);

        if (plant == null || !plant.isAlive()) {
            label.setText("empty plot");
            label.setTextFill(Color.web("#f0e6d2"));
            cell.setStyle(EMPTY_CELL_STYLE);
            clearCellTooltip(cell);
            return;
        }

        String name = plant.getType().getName();
        int health = plant.getHealth();
        label.setText(healthIcon(health) + " " + name + "\nHP " + health);
        label.setTextFill(Color.WHITE);
        cell.setStyle(plantCellStyle(health));

        String tooltipText = String.format(
            "%s at (%d,%d)%nHealth: %d%nWater: %d / %d%nNutrients: %d%nSoil moisture: %d%nStage: %s",
            name, row, col, health, plant.getWaterLevel(), plant.getType().getWaterRequirement(),
            plot.getNutrientLevel(), plot.getSoilMoisture(), plant.getStage()
        );
        Tooltip tooltip = new Tooltip(tooltipText);
        clearCellTooltip(cell);
        cell.getProperties().put("cellTooltip", tooltip);
        Tooltip.install(cell, tooltip);
    }

    private void clearCellTooltip(StackPane cell) {
        Object existing = cell.getProperties().remove("cellTooltip");
        if (existing instanceof Tooltip) {
            Tooltip.uninstall(cell, (Tooltip) existing);
        }
    }

    // Healthy = lush green, medium = yellowing, critical = wilting red — as a soil-bordered gradient tile.
    private String plantCellStyle(int health) {
        String fill;
        if (health >= 60) {
            fill = "linear-gradient(to bottom, #66bb6a, #2e7d32)";
        } else if (health >= 30) {
            fill = "linear-gradient(to bottom, #ffee58, #f9a825)";
        } else {
            fill = "linear-gradient(to bottom, #ef5350, #b71c1c)";
        }
        return "-fx-background-color: " + fill + "; -fx-border-color: #4a2e15; -fx-border-width: 1.5; "
            + "-fx-background-radius: 6; -fx-border-radius: 6;";
    }

    private String healthIcon(int health) {
        if (health >= 60) {
            return "🌿"; // herb / lush plant
        }
        if (health >= 30) {
            return "🌱"; // seedling / stressed
        }
        return "🥀"; // wilted flower / critical
    }

    private void refreshStatus() {
        if (!session.isInitialized()) {
            wateringStatus.setText("Watering: not initialized");
            climateStatus.setText("Climate: not initialized");
            pestStatus.setText("Pest control: not initialized");
            fertilizerStatus.setText("Fertilizer: not initialized");
            return;
        }

        // Read the persisted last-day snapshots: the engine resets live per-day state inside the
        // same tick, so these reflect what actually happened on the most recent event.
        WateringSystem watering = session.getWateringSystem();
        String rainStatus;
        if (watering.rainedLastDay()) {
            rainStatus = "rain absorbed last day";
        } else if (watering.getLastSprinkledCount() > 0) {
            rainStatus = "sprinklers watered " + watering.getLastSprinkledCount() + " plot(s)";
        } else {
            rainStatus = "monitoring soil moisture";
        }
        wateringStatus.setText("Watering: active — " + rainStatus);

        ClimateSystem climate = session.getClimateSystem();
        climateStatus.setText("Climate: " + climate.getLastTempF() + "°F"
            + (climate.isLastHeatingActive() ? " (heating on)" : "")
            + (climate.isLastCoolingActive() ? " (cooling on)" : ""));

        List<String> active = session.getPestControlSystem().getLastActiveParasites();
        if (active.isEmpty()) {
            pestStatus.setText("Pest control: no active infestations");
        } else {
            pestStatus.setText("Pest control: treating " + String.join(", ", active));
        }

        int treated = session.getFertilizerSystem().getPlotsTreatedToday();
        if (treated > 0) {
            fertilizerStatus.setText("Fertilizer: active — treated " + treated + " plot(s) today");
        } else {
            fertilizerStatus.setText("Fertilizer: monitoring nutrient levels");
        }
    }

    private void refreshHeader() {
        if (!session.isInitialized()) {
            dayLabel.setText("Day: —");
            aliveLabel.setText("Plants alive: —");
            return;
        }
        dayLabel.setText("Day: " + session.getEngine().getCurrentDay());
        aliveLabel.setText("Plants alive: " + session.getGarden().getLivingCount());
    }

    private void refreshLog() {
        try {
            Path logPath = Path.of("log.txt");
            if (!Files.exists(logPath)) {
                logArea.setText("(log.txt not created yet — click Initialize)");
                return;
            }
            List<String> lines = Files.readAllLines(logPath);
            int from = Math.max(0, lines.size() - LOG_TAIL_LINES);
            logArea.setText(String.join("\n", lines.subList(from, lines.size())));
            logArea.setScrollTop(Double.MAX_VALUE);
        } catch (IOException e) {
            logArea.setText("Unable to read log.txt: " + e.getMessage());
        }
    }

    private void refreshActionStates() {
        boolean ready = session.isInitialized();
        startPauseButton.setDisable(!ready);
    }

    private void showHelp() {
        String content;
        try {
            Path manual = Path.of(USER_MANUAL_PATH);
            if (Files.exists(manual)) {
                content = Files.readString(manual);
            } else {
                content = "User manual not found at " + USER_MANUAL_PATH + ".\n"
                    + "See docs/log-guide.md for log format details.";
            }
        } catch (IOException e) {
            content = "Could not read user manual: " + e.getMessage();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Garden Simulation Help");
        alert.setHeaderText("User Manual");
        alert.getDialogPane().setPrefSize(640, 480);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
