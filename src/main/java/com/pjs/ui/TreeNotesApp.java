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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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

    private static final DataFormat TREE_ITEM_FORMAT = new DataFormat("com.pjs.tree.drag");

    private Config config = new Config();
    private TreeView<TreeItemData> fileTree;
    private Label statusLabel;
    private WebRichTextEditor editor;
    private final FileSystemManager fileSystemManager;
    private TreeItem<TreeItemData> draggedItem;

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
        String css = getClass().getResource("/com/pjs/styles.css") == null
                ? null
                : getClass().getResource("/com/pjs/styles.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("ArboNotes");
        primaryStage.setScene(scene);
        try {
            primaryStage.getIcons().add(new Image(
                    getClass().getResourceAsStream("/icons/arbonote.png")));
        } catch (Exception ignored) {
            // Icon is optional; ignore if not present.
        }
        primaryStage.show();

        TreeItem<TreeItemData> firstItem = fileTree.getRoot();
        if (firstItem != null) {
            fileTree.getSelectionModel().select(firstItem);
        }
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

            cell.setOnDragDetected(event -> {
                TreeItem<TreeItemData> item = cell.getTreeItem();
                if (item == null || item == fileTree.getRoot()) return;

                draggedItem = item;
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(TREE_ITEM_FORMAT, item.getValue().getFileName());
                db.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (draggedItem == null) return;
                TreeItem<TreeItemData> target = cell.getTreeItem();
                if (target == null || target == draggedItem) return;
                if (isDescendant(draggedItem, target)) return;

                boolean onEdge = isOnEdge(cell, event.getY());
                if (onEdge && target.getParent() == null && target == fileTree.getRoot()) return;

                cell.getStyleClass().remove("drag-over");
                cell.getStyleClass().remove("drag-over-edge");
                cell.getStyleClass().add(onEdge ? "drag-over-edge" : "drag-over");

                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            });

            cell.setOnDragEntered(event -> {
                if (draggedItem == null) return;
                TreeItem<TreeItemData> target = cell.getTreeItem();
                if (target == null || target == draggedItem || isDescendant(draggedItem, target)) return;

                boolean onEdge = isOnEdge(cell, event.getY());
                if (onEdge && target.getParent() == null && target == fileTree.getRoot()) return;

                cell.getStyleClass().add(onEdge ? "drag-over-edge" : "drag-over");
            });

            cell.setOnDragExited(event -> {
                cell.getStyleClass().remove("drag-over");
                cell.getStyleClass().remove("drag-over-edge");
            });

            cell.setOnDragDropped(event -> {
                if (draggedItem == null) return;
                TreeItem<TreeItemData> target = cell.getTreeItem();
                if (target == null || target == draggedItem) return;
                if (isDescendant(draggedItem, target)) return;

                TreeItem<TreeItemData> oldParent = draggedItem.getParent();
                boolean onEdge = isOnEdge(cell, event.getY());

                if (onEdge) {
                    TreeItem<TreeItemData> newParent = target.getParent() != null ? target.getParent() : fileTree.getRoot();
                    oldParent.getChildren().remove(draggedItem);
                    int dropIndex = newParent.getChildren().indexOf(target);
                    boolean bottomEdge = isOnBottomEdge(cell, event.getY());
                    if (bottomEdge) {
                        dropIndex++;
                    }
                    newParent.getChildren().add(dropIndex, draggedItem);
                    newParent.setExpanded(true);
                } else {
                    oldParent.getChildren().remove(draggedItem);
                    target.getChildren().add(draggedItem);
                    target.setExpanded(true);
                }

                fileSystemManager.saveTreeToJson(fileTree.getRoot());

                TreeItem<TreeItemData> droppedItem = draggedItem;
                draggedItem = null;
                fileTree.getSelectionModel().select(droppedItem);
                cell.getStyleClass().remove("drag-over");
                cell.getStyleClass().remove("drag-over-edge");
                event.setDropCompleted(true);
                event.consume();
            });

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

    private static final double EDGE_THRESHOLD = 0.25;

    private boolean isDescendant(TreeItem<TreeItemData> ancestor, TreeItem<TreeItemData> node) {
        TreeItem<TreeItemData> current = node.getParent();
        while (current != null) {
            if (current == ancestor) return true;
            current = current.getParent();
        }
        return false;
    }

    private boolean isOnEdge(TreeCell<TreeItemData> cell, double y) {
        return y < cell.getHeight() * EDGE_THRESHOLD || y > cell.getHeight() * (1 - EDGE_THRESHOLD);
    }

    private boolean isOnBottomEdge(TreeCell<TreeItemData> cell, double y) {
        return y > cell.getHeight() * (1 - EDGE_THRESHOLD);
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("No file open");
        statusLabel.setFont(Font.font(11));
        HBox box = new HBox(statusLabel);
        box.getStyleClass().add("status-bar");
        box.setPadding(new Insets(4, 8, 4, 8));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        return box;
    }

    @Override
    public void stop() {
        Platform.exit();
    }

}