package com.csen275.garden.ui;

import com.csen275.garden.domain.plant.PlantInstance;
import com.csen275.garden.domain.plant.PlantType;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddPlantDialog {

    private AddPlantDialog() {
    }

    public static Optional<PlantInstance> show() {
        Dialog<PlantInstance> dialog = new Dialog<>();
        dialog.setTitle("Add Plant");
        dialog.setHeaderText("Configure a new plant for the garden grid.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Rose");

        TextField waterField = new TextField("10");
        waterField.setPromptText("Daily water need");

        TextField parasiteField = new TextField("aphid");
        parasiteField.setPromptText("Comma-separated, e.g. aphid, spider_mite");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Water requirement:"), 0, 1);
        grid.add(waterField, 1, 1);
        grid.add(new Label("Parasites:"), 0, 2);
        grid.add(parasiteField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                return null;
            }
            int waterRequirement;
            try {
                waterRequirement = Integer.parseInt(waterField.getText().trim());
            } catch (NumberFormatException e) {
                return null;
            }
            List<String> parasites = parseParasites(parasiteField.getText());
            PlantType type = new PlantType(name, waterRequirement, 2, parasites);
            return type.createInstance();
        });

        return dialog.showAndWait();
    }

    private static List<String> parseParasites(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
