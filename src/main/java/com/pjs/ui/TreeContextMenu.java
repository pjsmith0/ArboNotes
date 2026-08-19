package com.pjs.ui;

import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;
import com.pjs.util.Utils;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;

public class TreeContextMenu extends ContextMenu {

    private final FileSystemManager fileSystemManager;
    private final TreeCell<TreeItemData> cell;

    public TreeContextMenu(FileSystemManager fileSystemManager, TreeCell<TreeItemData> cell) {
        this.fileSystemManager = fileSystemManager;
        this.cell = cell;

        initialize();
    }

    private void initialize() {

        MenuItem create = new MenuItem("Create...");
        //create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        create.setOnAction(event -> {

            String rawTreeItemName = Dialogs.showTextInput("Enter a name...", null, cell.getScene().getWindow());
            if (!StringUtils.isEmpty(rawTreeItemName)) {
                String location = fileSystemManager.createLocation();
                String treeItemName = Utils.sanitize(rawTreeItemName);

                TreeItemData treeItemData = TreeItemData.builder()
                        .nodeName(treeItemName)
                        .fileName(location)
                        .build();

                cell.getTreeItem().getChildren().add(new TreeItem<>(treeItemData));

                fileSystemManager.saveTreeToJson(cell.getTreeView().getRoot());

                MultipleSelectionModel<TreeItem<TreeItemData>> msm = cell.getTreeView().getSelectionModel();
                TreeItem<TreeItemData> newTreeItem = cell.getTreeItem().getChildren().stream()
                        .filter(item -> item.getValue() == treeItemData)
                        .findFirst()
                        .orElseThrow();

                newTreeItem.getParent().setExpanded(true);
                int row = cell.getTreeView().getRow(newTreeItem);
                msm.select(row);
            }
        });
        this.getItems().add(create);

        MenuItem rename = new MenuItem("Rename");
        //rename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        rename.setOnAction(event -> {
            TreeItemData treeItemData = cell.getItem();
            String rawTreeItemName = Dialogs.showTextInput(
                    "Enter a new name...",
                    treeItemData.getNodeName(),
                    cell.getScene().getWindow());

            if (!StringUtils.isEmpty(rawTreeItemName)) {
                String treeItemName = Utils.sanitize(rawTreeItemName);
                treeItemData.setNodeName(treeItemName);

                //cell.getTreeView().
                //DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                //model.nodeChanged(selectedNode);

                fileSystemManager.saveTreeToJson(cell.getTreeView().getRoot());
            }
        });
        this.getItems().add(rename);

        //add(new JSeparator());

        MenuItem delete = new MenuItem("Delete");
        //delete.setEnabled(false);
        //delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        delete.setOnAction(event -> {
            System.out.println(cell.getTreeItem());

            if (Dialogs.showOptionDialog(Alert.AlertType.CONFIRMATION, "Are you sure?", cell.getScene().getWindow())) {
                MultipleSelectionModel<TreeItem<TreeItemData>> msm = cell.getTreeView().getSelectionModel();
                TreeItem<TreeItemData> selectedItem = msm.getSelectedItem();

                TreeItem<TreeItemData> parentItem = selectedItem.getParent();
                parentItem.getChildren().remove(selectedItem);

                int row = cell.getTreeView().getRow(parentItem);
                msm.select(row);

                TreeItemData treeItemData = selectedItem.getValue();
                fileSystemManager.deleteFile(treeItemData.getFileName());
                fileSystemManager.saveTreeToJson(cell.getTreeView().getRoot());
            }
        });
        this.getItems().add(delete);

        MenuItem search = new MenuItem("Search");
        //search.setEnabled(false);
        //search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        search.setOnAction(event -> {
            System.out.println(event);
            SearchPopup popup = new SearchPopup(fileSystemManager, cell.getTreeView(), cell.getTreeItem(), cell.getScene().getWindow());
            popup.show();
        });
        this.getItems().add(search);
    }
}
