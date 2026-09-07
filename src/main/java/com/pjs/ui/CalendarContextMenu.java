package com.pjs.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.pjs.util.FileSystemManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Window;

public class CalendarContextMenu extends ContextMenu {

    private static final DateTimeFormatter DAILY_NOTE_FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FileSystemManager fileSystemManager;
    private final CalendarView calendarView;
    private final LocalDate date;

    public CalendarContextMenu(FileSystemManager fileSystemManager,
                               CalendarView calendarView,
                               LocalDate date) {
        this.fileSystemManager = fileSystemManager;
        this.calendarView = calendarView;
        this.date = date;

        initialize();
    }

    private void initialize() {
        String fileName = DAILY_NOTE_FILE_FORMAT.format(date) + ".html";
        boolean hasNote = fileSystemManager.fileExists(fileName);

        MenuItem open = new MenuItem("Open note");
        open.setOnAction(event -> {
            Consumer<LocalDate> onDayClicked = calendarView.getOnDayClicked();
            if (onDayClicked != null) {
                onDayClicked.accept(date);
            }
        });

        MenuItem delete = new MenuItem("Delete note");
        delete.setDisable(!hasNote);
        delete.setOnAction(event -> {
            Window owner = calendarView.getScene() == null
                    ? null
                    : calendarView.getScene().getWindow();
            if (Dialogs.showOptionDialog(Alert.AlertType.CONFIRMATION,
                    "Delete note for " + date + "?", owner)) {
                fileSystemManager.deleteFile(fileName);
                calendarView.refresh();
            }
        });

        MenuItem copyDate = new MenuItem("Copy date");
        copyDate.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(date.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });

        if (hasNote) {
            getItems().addAll(open, delete, new SeparatorMenuItem(), copyDate);
        } else {
            getItems().addAll(open, copyDate);
        }
    }
}