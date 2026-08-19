package com.pjs.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class TreeItemData implements Serializable {
    private String nodeName;
    private String fileName;

    public String toString() {
        return nodeName;
    }
}
