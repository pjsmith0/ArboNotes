package com.pjs.util;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class ChunkedFileSearch {

    private static final int BUFFER_SIZE = 8192; // 8KB chunks

    public static boolean searchWithBufferedReader(Path file, String searchText) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()), BUFFER_SIZE)) {
            char[] buffer = new char[BUFFER_SIZE];
            StringBuilder overlap = new StringBuilder();
            int overlapSize = searchText.length() - 1;
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                String chunk = overlap.toString() + new String(buffer, 0, charsRead);
                if (chunk.contains(searchText)) {
                    return true;
                }
                if (chunk.length() > overlapSize) {
                    overlap = new StringBuilder(chunk.substring(chunk.length() - overlapSize));
                } else {
                    overlap = new StringBuilder(chunk);
                }
            }
        }
        return false;
    }

    @SneakyThrows
    public static boolean searchInChunksIgnoreCase(Path file, String searchText) {
        String lowerSearch = searchText.toLowerCase();
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()), BUFFER_SIZE)) {
            char[] buffer = new char[BUFFER_SIZE];
            StringBuilder overlap = new StringBuilder();
            int overlapSize = searchText.length() - 1;
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                String chunk = (overlap.toString() + new String(buffer, 0, charsRead)).toLowerCase();
                if (chunk.contains(lowerSearch)) {
                    return true;
                }
                if (chunk.length() > overlapSize) {
                    overlap = new StringBuilder(chunk.substring(chunk.length() - overlapSize));
                } else {
                    overlap = new StringBuilder(chunk);
                }
            }
        }
        return false;
    }
}
