package com.pjs.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.pjs.util.FileSystemManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

public class CalendarView extends VBox {

    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(
            this, "selectedDate", LocalDate.now());

    @Getter
    @Setter
    private Consumer<LocalDate> onDayClicked;

    private final Locale locale = Locale.getDefault();
    private final WeekFields weekFields = WeekFields.of(locale);
    private final Label titleLabel = new Label();
    private final GridPane weekdayRow = new GridPane();
    private final GridPane dayGrid = new GridPane();
    private final FileSystemManager fileSystemManager;

    private boolean refreshing = false;
    private YearMonth displayedMonth;

    public CalendarView(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
        this.displayedMonth = YearMonth.from(selectedDate.get());

        getStyleClass().add("calendar-view");
        setSpacing(4);

        getChildren().addAll(buildHeader(), buildWeekdayRow(), buildDayGrid());
        refresh();
    }

    private Node buildHeader() {
        Button prevYearButton = new Button("\u00AB");
        Button prevMonthButton = new Button("\u2039");
        Button nextMonthButton = new Button("\u203A");
        Button nextYearButton = new Button("\u00BB");
        Button todayButton = new Button("Today");

        prevYearButton.getStyleClass().add("calendar-nav-button");
        prevMonthButton.getStyleClass().add("calendar-nav-button");
        nextMonthButton.getStyleClass().add("calendar-nav-button");
        nextYearButton.getStyleClass().add("calendar-nav-button");
        todayButton.getStyleClass().add("calendar-nav-button");

        prevYearButton.setOnAction(e -> shiftMonths(-12));
        prevMonthButton.setOnAction(e -> shiftMonths(-1));
        nextMonthButton.setOnAction(e -> shiftMonths(1));
        nextYearButton.setOnAction(e -> shiftMonths(12));
        todayButton.setOnAction(e -> {
            selectedDate.set(LocalDate.now());
            displayedMonth = YearMonth.now();
            refresh();
        });

        titleLabel.getStyleClass().add("calendar-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        HBox prevGroup = new HBox(2, prevYearButton, prevMonthButton);
        HBox nextGroup = new HBox(2, nextMonthButton, nextYearButton, todayButton);
        prevGroup.setAlignment(Pos.CENTER_LEFT);
        nextGroup.setAlignment(Pos.CENTER_RIGHT);

        BorderPane header = new BorderPane();
        header.getStyleClass().add("calendar-header");
        header.setLeft(prevGroup);
        header.setCenter(titleLabel);
        header.setRight(nextGroup);

        return header;
    }

    private Node buildWeekdayRow() {
        weekdayRow.getStyleClass().add("calendar-weekdays");

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = weekFields.getFirstDayOfWeek().plus(i);
            Label label = new Label(day.getDisplayName(TextStyle.SHORT, locale));
            label.getStyleClass().add("calendar-weekday");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            weekdayRow.add(label, i, 0);
        }
        addColumnConstraints(weekdayRow);
        return weekdayRow;
    }

    private Node buildDayGrid() {
        dayGrid.getStyleClass().add("calendar-grid");
        addColumnConstraints(dayGrid);

        for (int row = 0; row < 6; row++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(100.0 / 6);
            rowConstraint.setVgrow(Priority.ALWAYS);
            dayGrid.getRowConstraints().add(rowConstraint);
        }
        return dayGrid;
    }

    private void addColumnConstraints(GridPane grid) {
        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }
    }

    private void shiftMonths(int months) {
        displayedMonth = displayedMonth.plusMonths(months);
        refresh();
    }

    public void refresh() {
        if (refreshing) return;
        refreshing = true;
        try {
            refreshTitle();
            refreshDays();
        } finally {
            refreshing = false;
        }
    }

    private void refreshTitle() {
        Month month = displayedMonth.getMonth();
        titleLabel.setText(month.getDisplayName(TextStyle.FULL, locale)
                + " " + displayedMonth.getYear());
    }

    private void refreshDays() {
        List<LocalDate> datesWithNotesList = fileSystemManager.getDatesWithFiles(displayedMonth);

        dayGrid.getChildren().clear();

        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int leadingBlanks = (firstOfMonth.getDayOfWeek().getValue()
                - weekFields.getFirstDayOfWeek().getValue() + 7) % 7;
        LocalDate gridStart = firstOfMonth.minusDays(leadingBlanks);

        LocalDate today = LocalDate.now();
        LocalDate selected = selectedDate.get();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate date = gridStart.plusDays(row * 7L + col);

                Label cell = new Label(Integer.toString(date.getDayOfMonth()));
                cell.getStyleClass().add("calendar-day");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.setAlignment(Pos.CENTER);

                if (!date.getMonth().equals(displayedMonth.getMonth())) {
                    cell.getStyleClass().add("calendar-day-other");
                }
                if (date.equals(today)) {
                    cell.getStyleClass().add("calendar-day-today");
                }
                if (date.equals(selected)) {
                    cell.getStyleClass().add("calendar-day-selected");
                }
                if (datesWithNotesList.contains(date)) {
                    cell.getStyleClass().add("calendar-day-with-note");
                }

                cell.setOnMouseClicked(e -> {
                    if (e.getButton() != MouseButton.PRIMARY) {
                        return;
                    }
                    selectedDate.set(date);
                    cell.requestFocus();
                    refreshDays();
                    if (onDayClicked != null) {
                        onDayClicked.accept(date);
                    }
                });

                cell.setContextMenu(new CalendarContextMenu(fileSystemManager, this, date));

                cell.setFocusTraversable(true);
                cell.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (wasFocused && !isFocused) {
                        refresh();
                    }
                });

                dayGrid.add(cell, col, row);
            }
        }
    }
}
