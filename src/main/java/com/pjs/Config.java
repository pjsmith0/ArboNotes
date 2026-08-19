package com.pjs;

import lombok.Data;

import java.nio.file.Path;

@Data
public class Config {
    private String rootPath = "arbonote_data";
    private String fileExtention = ".html";
    private String treeFileName = "tree.json";

    public String getRootPath() {
        return Path.of(System.getProperty("user.home"), rootPath).toString();
    }
}
