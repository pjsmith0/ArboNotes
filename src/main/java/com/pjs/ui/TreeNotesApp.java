package com.pjs.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjs.Config;
import com.pjs.model.TreeHierarchyData;
import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class TreeNotesApp extends Application  {

    private Config config = new Config();
    private TreeView<TreeItemData> fileTree;
    private Label statusLabel;
    private WebRichTextEditor editor;
    private final FileSystemManager fileSystemManager;

    public TreeNotesApp() {
        this.fileSystemManager = new FileSystemManager(config);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setTop(buildToolbar(primaryStage));
        root.setCenter(buildSplitPane(primaryStage));
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1200, 800);
        String css = getClass().getResource("/com/fileeditor/app/pjs/styles.css") == null
                ? null
                : getClass().getResource("/com/fileeditor/app/pjs/styles.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("File Tree Rich Text Editor");
        primaryStage.setScene(scene);
        try {
            primaryStage.getIcons().add(new Image(
                    getClass().getResourceAsStream("/com/fileeditor/app/icon.png")));
        } catch (Exception ignored) {
            // Icon is optional; ignore if not present.
        }
        primaryStage.show();
    }

    private MenuBar buildToolbar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setMinWidth(100);

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(event -> {
            System.exit(0);
        });
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(exitMenuItem);

        MenuItem aboutMenuItem = new MenuItem("About");
//        aboutMenuItem.addActionListener(e -> {
//            showAboutDialog(TreeNotesApp.this);
//        });
        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(aboutMenuItem);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(helpMenu);

        return menuBar;
    }

    @SneakyThrows
    private SplitPane buildSplitPane(Stage stage) {
        fileTree = new TreeView<>();
        fileTree.setShowRoot(true);
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // TODO: do drag & drop handlers

        fileTree.setCellFactory(tv -> {
            TreeCell<TreeItemData> cell = new TreeCell<>() {
                @Override
                protected void updateItem(TreeItemData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getNodeName());
                    }
                }
            };

            // Create the ContextMenu
            TreeContextMenu contextMenu = new TreeContextMenu(fileSystemManager, cell);

            // Bind context menu to cell, but only display it if the cell is not empty
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });

            return cell;
        });

        ObjectMapper mapper = new ObjectMapper();
        TreeHierarchyData rootPageNode = mapper.readValue(
                Path.of(config.getRootPath(), config.getTreeFileName()).toFile(),
                TreeHierarchyData.class
        );

        TreeItem<TreeItemData> rootItem = toTreeItem(rootPageNode);
        rootItem.setExpanded(true);
        fileTree.setRoot(rootItem);

        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {

            TreeItemData prevSelectedData = Optional.ofNullable(oldV).map(TreeItem::getValue).orElseGet(() -> null);
            TreeItemData newSelectedData = Optional.ofNullable(newV).map(TreeItem::getValue).orElseGet(() -> null);

            if (prevSelectedData != null) {
                String html = editor.getHtmlText();
                fileSystemManager.saveTreeItem(prevSelectedData.getFileName(), html);
            }

            if (newSelectedData != null) {
                statusLabel.setText(" Selected: %s   >>   FileName: %s".formatted(
                        newSelectedData.getNodeName(),
                        newSelectedData.getFileName()));

                String htmlToSet = fileSystemManager.loadData(newSelectedData.getFileName());
                editor.setHtmlText(htmlToSet);
            }

        });

        fileTree.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                TreeItem<TreeItemData> item = fileTree.getSelectionModel().getSelectedItem();
                if (item != null && item.getValue() != null) {
                    String html = fileSystemManager.loadData(item.getValue().getFileName());
                    editor.setHtmlText(html);
                }
            }
        });

        editor = new WebRichTextEditor();
        editor.setPrefWidth(760);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().addAll(fileTree, editor);
        split.setDividerPositions(0.28);
        SplitPane.setResizableWithParent(fileTree, Boolean.TRUE);
        return split;
    }

    private TreeItem<TreeItemData> toTreeItem(TreeHierarchyData node) {
        TreeItem<TreeItemData> item = new TreeItem<>(
                TreeItemData.builder()
                        .nodeName(node.getNodeName())
                        .fileName(node.getFileName())
                        .build()
        );
        node.getChildren().forEach(child -> item.getChildren().add(toTreeItem(child)));
        return item;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("No file open");
        statusLabel.setFont(Font.font(11));
        HBox box = new HBox(statusLabel);
        box.setPadding(new Insets(4, 8, 4, 8));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        return box;
    }

    @Override
    public void stop() {
        Platform.exit();
    }

}