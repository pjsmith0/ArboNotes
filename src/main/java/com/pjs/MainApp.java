package com.pjs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

/**
 * Main entry point.
 *
 * Layout:
 *   - Left: a TreeView<File> showing the filesystem (lazily loaded).
 *   - Right: an HTMLEditor providing a rich-text formatting toolbar
 *            (bold, italic, underline, lists, alignment, color, etc.).
 *
 * Double-clicking a text-like file in the tree loads its contents into
 * the editor. The Save button writes the editor's HTML content back to
 * the currently open file (or lets you choose a new one).
 */
public class MainApp extends Application {

    private TreeView<File> fileTree;
    private HTMLEditor editor;
    private Label statusLabel;
    private File currentFile;

    // Extensions we're willing to open as text/HTML into the rich editor.
    private static final List<String> OPENABLE_EXTENSIONS =
            List.of("txt", "html", "htm", "md", "log", "xml", "css", "csv");

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

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------

    private HBox buildToolbar(Stage stage) {
        Button openFolder = new Button("Open Folder…");
        openFolder.setOnAction(e -> chooseRootFolder(stage));

        Button newFile = new Button("New");
        newFile.setOnAction(e -> newFile());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveCurrentFile(stage));

        Button saveAsButton = new Button("Save As…");
        saveAsButton.setOnAction(e -> saveAs(stage));

        HBox bar = new HBox(8, openFolder, newFile, saveButton, saveAsButton);
        bar.setPadding(new Insets(8));
        return bar;
    }

    private SplitPane buildSplitPane(Stage stage) {
        fileTree = new TreeView<>();
        fileTree.setShowRoot(true);
        fileTree.setCellFactory(tv -> new FileTreeCell());
        seedTreeWithDefaultRoots();

        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.getValue() != null && newV.getValue().isFile()) {
                statusLabel.setText(newV.getValue().getAbsolutePath());
            }
        });

        fileTree.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                TreeItem<File> item = fileTree.getSelectionModel().getSelectedItem();
                if (item != null && item.getValue() != null && item.getValue().isFile()) {
                    openFile(item.getValue());
                }
            }
        });

        editor = new HTMLEditor();
        editor.setPrefWidth(760);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().addAll(fileTree, editor);
        split.setDividerPositions(0.28);
        SplitPane.setResizableWithParent(fileTree, Boolean.TRUE);
        return split;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("No file open");
        statusLabel.setFont(Font.font(11));
        HBox box = new HBox(statusLabel);
        box.setPadding(new Insets(4, 8, 4, 8));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        return box;
    }

    // ------------------------------------------------------------------
    // Tree population
    // ------------------------------------------------------------------

    private void seedTreeWithDefaultRoots() {
        String home = System.getProperty("user.home");
        File rootDir = new File(home != null ? home : ".");
        TreeItem<File> rootItem = createNode(rootDir);
        rootItem.setExpanded(true);
        fileTree.setRoot(rootItem);
    }

    private void chooseRootFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder to browse");
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            TreeItem<File> rootItem = createNode(dir);
            rootItem.setExpanded(true);
            fileTree.setRoot(rootItem);
        }
    }

    /** Creates a lazily-populated tree node for the given file/directory. */
    private TreeItem<File> createNode(final File file) {
        return new TreeItem<>(file) {
            private boolean loaded = false;

            @Override
            public boolean isLeaf() {
                return file.isFile();
            }

            @Override
            public javafx.collections.ObservableList<TreeItem<File>> getChildren() {
                if (!loaded) {
                    loaded = true;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }
        };
    }

    private List<TreeItem<File>> buildChildren(TreeItem<File> parent) {
        File dir = parent.getValue();
        File[] children = dir.listFiles();
        if (children == null) {
            return List.of();
        }
        return java.util.Arrays.stream(children)
                .sorted(Comparator.comparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::createNode)
                .toList();
    }

    /** Renders a file name (not the full path) with a folder/file distinction via CSS class. */
    private static class FileTreeCell extends TreeCell<File> {
        @Override
        protected void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll("tree-file", "tree-dir");
            } else {
                setText(item.getName().isEmpty() ? item.getAbsolutePath() : item.getName());
                getStyleClass().removeAll("tree-file", "tree-dir");
                getStyleClass().add(item.isDirectory() ? "tree-dir" : "tree-file");
            }
        }
    }

    // ------------------------------------------------------------------
    // File open / save
    // ------------------------------------------------------------------

    private void newFile() {
        currentFile = null;
        editor.setHtmlText("");
        statusLabel.setText("New unsaved document");
    }

    private void openFile(File file) {
        String ext = extensionOf(file.getName());
        if (!OPENABLE_EXTENSIONS.contains(ext)) {
            statusLabel.setText("Cannot open (unsupported type): " + file.getName());
            return;
        }
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            boolean isHtml = ext.equals("html") || ext.equals("htm");
            editor.setHtmlText(isHtml ? content : "<html><body><pre>" + escapeHtml(content) + "</pre></body></html>");
            currentFile = file;
            statusLabel.setText("Opened: " + file.getAbsolutePath());
        } catch (IOException ex) {
            showError("Could not open file", ex.getMessage());
        }
    }

    private void saveCurrentFile(Stage stage) {
        if (currentFile == null) {
            saveAs(stage);
            return;
        }
        writeToFile(currentFile);
    }

    private void saveAs(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML files", "*.html", "*.htm"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All files", "*.*"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            currentFile = file;
            writeToFile(file);
            refreshTreeRoot();
        }
    }

    private void writeToFile(File file) {
        try {
            Files.writeString(file.toPath(), editor.getHtmlText(), StandardCharsets.UTF_8);
            statusLabel.setText("Saved: " + file.getAbsolutePath());
        } catch (IOException ex) {
            showError("Could not save file", ex.getMessage());
        }
    }

    private void refreshTreeRoot() {
        if (fileTree.getRoot() != null) {
            TreeItem<File> newRoot = createNode(fileTree.getRoot().getValue());
            newRoot.setExpanded(true);
            fileTree.setRoot(newRoot);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
