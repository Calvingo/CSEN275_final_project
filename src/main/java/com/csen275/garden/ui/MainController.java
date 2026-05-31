package com.csen275.garden.ui;

import com.csen275.garden.domain.garden.GardenGrid;
import com.csen275.garden.domain.garden.Plot;
import com.csen275.garden.domain.plant.PlantInstance;
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
        root.setTop(buildToolbar());
        root.setCenter(buildGardenView());
        root.setRight(buildStatusPanel());
        root.setBottom(buildLogPanel());
        return root;
    }

    private HBox buildToolbar() {
        Button initializeButton = new Button("Initialize");
        initializeButton.setOnAction(e -> handleInitialize());

        startPauseButton = new Button("Start");
        startPauseButton.setTooltip(new Tooltip("Auto-advance days with random rain, heat waves, or parasite outbreaks"));
        startPauseButton.setOnAction(e -> toggleSimulation());

        Button manualWaterButton = new Button("Manual Water");
        manualWaterButton.setOnAction(e -> handleManualWater());

        Button manualFertilizerButton = new Button("Apply Fertilizer");
        manualFertilizerButton.setOnAction(e -> handleManualFertilizer());

        Button addPlantButton = new Button("Add Plant");
        addPlantButton.setOnAction(e -> handleAddPlant());

        Button rainButton = new Button("Rain (15)");
        rainButton.setOnAction(e -> handleRain(15));

        Button heatButton = new Button("Heat (105°F)");
        heatButton.setOnAction(e -> handleTemperature(105));

        Button parasiteButton = new Button("Parasite (aphid)");
        parasiteButton.setOnAction(e -> handleParasite("aphid"));

        Button stateButton = new Button("Log State");
        stateButton.setOnAction(e -> handleLogState());

        Button helpButton = new Button("Help");
        helpButton.setOnAction(e -> showHelp());

        dayLabel = new Label("Day: —");
        aliveLabel = new Label("Plants alive: —");

        Label speedLabel = new Label("Speed:");
        Slider speedSlider = new Slider(0.5, 4.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setBlockIncrement(0.5);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateSimulationSpeed(newVal.doubleValue()));

        HBox toolbar = new HBox(8,
            initializeButton, startPauseButton, manualWaterButton, manualFertilizerButton, addPlantButton,
            rainButton, heatButton, parasiteButton, stateButton, helpButton,
            new Region(), dayLabel, aliveLabel, speedLabel, speedSlider
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(toolbar.getChildren().get(toolbar.getChildren().size() - 4), Priority.ALWAYS);
        return toolbar;
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
        return scrollPane;
    }

    private StackPane createEmptyCell(int row, int col) {
        Label label = new Label("Empty");
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#555555"));

        StackPane cell = new StackPane(label);
        cell.setMinSize(110, 90);
        cell.setPrefSize(110, 90);
        cell.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #bdbdbd; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
        cell.setUserData(new int[] { row, col });
        return cell;
    }

    private VBox buildStatusPanel() {
        wateringStatus = new Label("Watering: not initialized");
        climateStatus = new Label("Climate: not initialized");
        pestStatus = new Label("Pest control: not initialized");
        fertilizerStatus = new Label("Fertilizer: not initialized");

        wateringStatus.setWrapText(true);
        climateStatus.setWrapText(true);
        pestStatus.setWrapText(true);
        fertilizerStatus.setWrapText(true);

        Label title = new Label("Subsystems");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox panel = new VBox(12, title, wateringStatus, climateStatus, pestStatus, fertilizerStatus);
        panel.setPadding(new Insets(10, 10, 10, 20));
        panel.setMinWidth(220);
        panel.setMaxWidth(260);
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
        panel.setPadding(new Insets(10, 0, 0, 0));
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
            label.setText("Empty");
            label.setTextFill(Color.web("#666666"));
            cell.setStyle("-fx-background-color: #ececec; -fx-border-color: #bdbdbd; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
            clearCellTooltip(cell);
            return;
        }

        String name = plant.getType().getName();
        int health = plant.getHealth();
        label.setText(name + "\nHP " + health);
        label.setTextFill(Color.web("#1b1b1b"));
        cell.setStyle("-fx-background-color: " + toHex(healthColor(health)) + "; -fx-border-color: #616161; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

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

    private Color healthColor(int health) {
        if (health >= 60) {
            return Color.web("#81c784");
        }
        if (health >= 30) {
            return Color.web("#fff176");
        }
        return Color.web("#e57373");
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    private void refreshStatus() {
        if (!session.isInitialized()) {
            wateringStatus.setText("Watering: not initialized");
            climateStatus.setText("Climate: not initialized");
            pestStatus.setText("Pest control: not initialized");
            fertilizerStatus.setText("Fertilizer: not initialized");
            return;
        }

        String rainStatus = session.getWateringSystem().isRainedToday() ? "rain absorbed today" : "monitoring soil moisture";
        wateringStatus.setText("Watering: active — " + rainStatus);

        climateStatus.setText("Climate: " + session.getClimateSystem().getCurrentTempF() + "°F"
            + (session.getClimateSystem().isHeatingActive() ? " (heating on)" : "")
            + (session.getClimateSystem().isCoolingActive() ? " (cooling on)" : ""));

        List<String> active = session.getPestControlSystem().getActiveParasites();
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
