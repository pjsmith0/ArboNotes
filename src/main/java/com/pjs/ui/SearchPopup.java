package com.pjs.ui;

import com.pjs.model.SearchItemData;
import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class SearchPopup {

    private final FileSystemManager fileSystemManager;
    private final TreeView<TreeItemData> pagesTree;
    private final TreeItem<TreeItemData> selectedItem;
    private final Stage popupStage;
    private final javafx.scene.control.TextField searchField;
    private final ListView<SearchItemData> resultsList;
    private final Button searchButton;

    public SearchPopup(FileSystemManager fileSystemManager,
                       TreeView<TreeItemData> pagesTree,
                       TreeItem<TreeItemData> selectedItem,
                       Window owner) {
        this.fileSystemManager = fileSystemManager;
        this.pagesTree = pagesTree;
        this.selectedItem = selectedItem;

        popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(owner);
        popupStage.setTitle("Search Files");

        searchField = new javafx.scene.control.TextField();
        searchField.setPrefColumnCount(20);
        searchButton = new Button("Search");
        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        resultsList = new ListView<>();
        resultsList.getSelectionModel().selectFirst();
        resultsList.setPrefSize(350, 200);

        resultsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SearchItemData item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNodeName());
            }
        });

        resultsList.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                SearchItemData clickedItem = resultsList.getSelectionModel().getSelectedItem();
                if (clickedItem != null && clickedItem.getTreeItem() instanceof TreeItem<?> treeNode) {
                    TreeItem<TreeItemData> target = (TreeItem<TreeItemData>) treeNode;
                    pagesTree.getSelectionModel().select(target);
                    pagesTree.scrollTo(pagesTree.getRow(target));
                    popupStage.close();
                }
            }
        });

        Label searchLabel = new Label("Search for text in files:");
        Label resultsLabel = new Label("Files containing search text:");

        VBox layout = new VBox(10,
                searchLabel,
                searchBox,
                resultsLabel,
                resultsList);
        layout.setPadding(new Insets(15));

        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        popupStage.setScene(new Scene(layout, 400, 350));
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }

        resultsList.getItems().clear();

        List<SearchItemData> locationsToSearch = new ArrayList<>();
        gatherFileNames(locationsToSearch, selectedItem);

        List<SearchItemData> foundItems =
                fileSystemManager.searchForTextInsideList(locationsToSearch, searchText);

        resultsList.getItems().addAll(foundItems);
    }

    private void gatherFileNames(List<SearchItemData> locationsToSearch, TreeItem<TreeItemData> treeItem) {
        TreeItemData data = treeItem.getValue();
        if (data == null) {
            return;
        }

        locationsToSearch.add(SearchItemData.builder()
                .fileName(data.getFileName())
                .nodeName(data.getNodeName())
                .treeItem(treeItem)
                .build());

        for (TreeItem<TreeItemData> child : treeItem.getChildren()) {
            TreeItemData childData = child.getValue();
            if (childData != null) {
                locationsToSearch.add(SearchItemData.builder()
                        .fileName(childData.getFileName())
                        .nodeName(childData.getNodeName())
                        .treeItem(child)
                        .build());
            }

            if (!child.getChildren().isEmpty()) {
                gatherFileNames(locationsToSearch, child);
            }
        }
    }

    public void show() {
        popupStage.showAndWait();
    }

    public SearchItemData getSelectedFile() {
        return resultsList.getSelectionModel().getSelectedItem();
    }
}
