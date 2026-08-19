package com.pjs.util;

import java.text.Normalizer;
import java.util.Set;

public class Utils {

    private static final Set<String> WINDOWS_RESERVED = Set.of(
            "CON","PRN","AUX","NUL",
            "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
            "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
    );

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) return "dir";

        // Normalize unicode (é -> e etc.)
        String name = Normalizer.normalize(input, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");

        // Replace invalid characters (Windows forbidden + general unsafe)
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");

        // Replace control chars
        name = name.replaceAll("\\p{Cntrl}", "_");

        // Collapse whitespace
        //name = name.trim().replaceAll("\\s+", "_");

        // Remove trailing dots or spaces (Windows restriction)
        name = name.replaceAll("[. ]+$", "");

        // Keep only safe characters (optional but recommended)
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (name.isEmpty()) name = "dir";

        // Windows reserved filename check
        if (WINDOWS_RESERVED.contains(name.toUpperCase())) {
            name = "_" + name;
        }

        return name;
    }

}
