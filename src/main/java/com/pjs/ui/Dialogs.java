package com.pjs.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Optional;

public class Dialogs {

    public static void showDialog(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UTILITY);
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static boolean showOptionDialog(Alert.AlertType type, String message, Window owner) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UTILITY);
        alert.setHeaderText(message);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        alert.setOnShown(e -> {
            Window window = alert.getDialogPane().getScene().getWindow();
            center(window, owner);
            window.widthProperty().addListener((obs, o, n) -> center(window, owner));
            window.heightProperty().addListener((obs, o, n) -> center(window, owner));
        });

        return alert.showAndWait()
                .map(buttonType -> buttonType == yesButton)
                .orElseGet(() -> false);
    }

    public static String showTextInput(String message, String defaultValue, Window owner) {
        TextInputDialog dialog = defaultValue == null ?
                new TextInputDialog() :
                new TextInputDialog(defaultValue);

        dialog.initStyle(StageStyle.UTILITY);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setHeaderText(message);

        dialog.setOnShown(e -> {
            Window window = dialog.getDialogPane().getScene().getWindow();
            center(window, owner);
            window.widthProperty().addListener((obs, o, n) -> center(window, owner));
            window.heightProperty().addListener((obs, o, n) -> center(window, owner));
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);

    }

    private static void center(Window window, Window owner) {
        if (owner != null) {
            window.setX(owner.getX() + (owner.getWidth() - window.getWidth()) / 2);
            window.setY(owner.getY() + (owner.getHeight() - window.getHeight()) / 2);
            return;
        }
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getMinX() + (bounds.getWidth() - window.getWidth()) / 2);
        window.setY(bounds.getMinY() + (bounds.getHeight() - window.getHeight()) / 2);
    }

}
