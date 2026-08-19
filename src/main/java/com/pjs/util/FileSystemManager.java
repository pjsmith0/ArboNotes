package com.pjs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pjs.Config;
import com.pjs.model.SearchItemData;
import com.pjs.model.TreeItemData;
import com.pjs.model.TreeHierarchyData;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class FileSystemManager {

    private final ObjectMapper mapper;
    private final Config config;

    public FileSystemManager(Config config) {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);  // Pretty print

        this.config = config;

        File baseDir = new File(config.getRootPath());
        if (!baseDir.exists()) {
            System.out.printf("Base path [%s] does not exist...%n", config.getRootPath());
            System.exit(1);
        }

        // check if tree file exists
        Path treefilePath = Paths.get(config.getRootPath(), config.getTreeFileName());
        if (!Files.exists(treefilePath)) {
            saveTreeFile(TreeHierarchyData.builder()
                    .fileName("Root.html")
                    .nodeName("Home")
                    .build());
        }
    }

    public void saveTreeItem(String itemLocation, String htmlText) {
        Path path = getAbsoluteFileLocation(itemLocation);

        try {
            File file = path.toFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(htmlText);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public String loadData(String fileName) {
        File file = getAbsoluteFileLocation(fileName).toFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        return FileUtils.readFileToString(file, "UTF-8");
    }

    @SneakyThrows
    public String createLocation() {
        Path path = Paths.get(config.getRootPath(), UUID.randomUUID() + config.getFileExtention());
        File newFile = path.toFile();
        newFile.createNewFile();
        return newFile.getName();
    }

    private Path getAbsoluteFileLocation(String fullItemName) {
        return Paths.get(config.getRootPath(), fullItemName);
    }

    public void saveTreeToJson(TreeItem<TreeItemData> root) {
        TreeHierarchyData rootNode = treeItemToNode(root);
        saveTreeFile(rootNode);
    }

    private void saveTreeFile(TreeHierarchyData rootNode) {
        try {
            mapper.writeValue(new FileOutputStream(Path.of(config.getRootPath(), config.getTreeFileName()).toFile()), rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TreeHierarchyData treeItemToNode(TreeItem<TreeItemData> treeNode) {
        if (treeNode == null) return null;

        TreeHierarchyData node = TreeHierarchyData.builder()
                .nodeName(treeNode.getValue().getNodeName())
                .fileName(treeNode.getValue().getFileName())
                .build();

        ObservableList<TreeItem<TreeItemData>> children = treeNode.getChildren();
        for (TreeItem<TreeItemData> child : children) {
            node.getChildren().add(treeItemToNode(child));
        }
        return node;
    }

    @SneakyThrows
    public void deleteFile(String fileName) {
        FileUtils.delete(getAbsoluteFileLocation(fileName).toFile());
    }

    public List<SearchItemData> searchForTextInsideList(List<SearchItemData> locationsToSearch, String searchText) {
        return locationsToSearch.stream().filter(
                searchItemData -> ChunkedFileSearch.searchInChunksIgnoreCase(
                        Path.of(config.getRootPath(), searchItemData.getFileName()),
                        searchText))
                .toList();
    }

}
