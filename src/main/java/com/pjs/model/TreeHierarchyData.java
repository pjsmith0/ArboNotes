package com.pjs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeHierarchyData {
    private String nodeName;
    private String fileName;
    @Builder.Default
    private List<TreeHierarchyData> children = new ArrayList<>();
}
