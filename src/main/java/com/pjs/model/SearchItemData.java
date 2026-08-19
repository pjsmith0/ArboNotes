package com.pjs.model;

import javafx.scene.control.TreeItem;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class SearchItemData implements Serializable {
    private String nodeName;
    private String fileName;
    private TreeItem<TreeItemData> treeItem;
}
