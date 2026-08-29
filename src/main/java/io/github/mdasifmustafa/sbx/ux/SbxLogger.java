package io.github.mdasifmustafa.sbx.ux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class SbxLogger {

    public static final String GLOBAL_FILE_NAME = "sbx-global.log";
    public static final String INFO_FILE_NAME = "sbx-info.log";
    public static final String SUCCESS_FILE_NAME = "sbx-success.log";
    public static final String ERROR_FILE_NAME = "sbx-errors.log";
    public static final String WARNING_FILE_NAME = "sbx-warnings.log";
    public static final String HELP_FILE_NAME = "sbx-help.log";
    public static final String TIP_FILE_NAME = "sbx-tips.log";

    private SbxLogger() {
    }

    public static Path getGlobalLogFile(Path root) {
        return root.resolve(GLOBAL_FILE_NAME);
    }

    public static Path getGlobalErrorFile(Path root) {
        return root.resolve(ERROR_FILE_NAME);
    }

    public static List<String> readAll(Path root) {
        try {
            Path file = getGlobalLogFile(root);
            if (!Files.exists(file)) {
                return new ArrayList<>();
            }
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SBX global log", e);
        }
    }

    public static void write(Path root, String message) {
        info(root, message);
    }

    public static void info(Path root, String message) {
        append(root, INFO_FILE_NAME, "INFO", message);
        append(root, GLOBAL_FILE_NAME, "INFO", message);
    }

    public static void success(Path root, String message) {
        append(root, SUCCESS_FILE_NAME, "SUCCESS", message);
        append(root, GLOBAL_FILE_NAME, "SUCCESS", message);
    }

    public static void error(Path root, String message) {
        append(root, ERROR_FILE_NAME, "ERROR", message);
        append(root, GLOBAL_FILE_NAME, "ERROR", message);
    }

    public static void warning(Path root, String message) {
        append(root, WARNING_FILE_NAME, "WARNING", message);
        append(root, GLOBAL_FILE_NAME, "WARNING", message);
    }

    public static void help(Path root, String message) {
        append(root, HELP_FILE_NAME, "HELP", message);
        append(root, GLOBAL_FILE_NAME, "HELP", message);
    }

    public static void tip(Path root, String message) {
        append(root, TIP_FILE_NAME, "TIP", message);
        append(root, GLOBAL_FILE_NAME, "TIP", message);
    }

    private static void append(Path root, String fileName, String level, String message) {
        try {
            Path file = root.resolve(fileName);
            Files.createDirectories(root);
            String entry = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    + " [" + level + "] " + message;
            Files.writeString(
                    file,
                    entry + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write SBX log: " + fileName, e);
        }
    }
}
